package com.example.memolens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.memolens.databinding.FragmentCameraBinding;
import com.example.memolens.medication.MedicationFragment;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private Button captureButton;
    private TextToSpeech tts;
    FragmentCameraBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize views
//        previewView = binding.previewView;
        captureButton = binding.captureButton;
        Button medicationButton = binding.medicationButton;

        // Initialize Text-to-Speech
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // Request Camera Permissions if not granted
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 101);
//        } else {
//            startCamera();  // Start the camera if permission is granted
//        }

        // Capture button listener
        captureButton.setOnClickListener(v -> takePicture());

        // Medication button listener
        medicationButton.setOnClickListener(v -> {
            Fragment fragment = new MedicationFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error initializing camera", e);
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder().build(); // Initialize ImageCapture

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    // Capture the image and send it to the server
    private void takePicture() {
        if (imageCapture == null) {
            return;
        }

        // Create output file to hold the image
        File photoFile = new File(getContext().getExternalFilesDir(null), System.currentTimeMillis() + "_photo.jpg");

        // Create output options object to hold file and metadata
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take a picture and save it to the provided file
        imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(getContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Once the image is saved, send it to the server
                        sendImageToServer(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage(), exception);
                    }
                }
        );
    }

    // Convert image to Base64 and send it to the Flask backend
    private void sendImageToServer(File photoFile) {
        new Thread(() -> {
            try {
                // Convert the image to Base64
                byte[] imageBytes = Files.readAllBytes(photoFile.toPath());
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Create a JSON object with the Base64 image
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("image", base64Image);

                // Setup the HTTP connection
                URL url = new URL("http://your-server-ip:5000/analyze");  // Replace with your Flask server URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send the JSON data
                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write(jsonParam.toString());
                os.flush();
                os.close();

                // Get the response
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Handle the server response
                    getActivity().runOnUiThread(() -> handleServerResponse(response.toString()));

                } else {
                    Log.e("ServerError", "Server responded with code: " + responseCode);
                    getActivity().runOnUiThread(() -> speak("Server error. Please try again later."));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> speak("Network or processing error occurred. Please try again."));
            }
        }).start();
    }

    // Handle the server response and announce the recognized person
    private void handleServerResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.has("person")) {
                String recognizedPerson = jsonResponse.getString("person");
                speak("Recognized person: " + recognizedPerson);
            } else {
                speak("No match found.");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            speak("An error occurred while processing the response.");
        }
    }

    // Text-to-Speech function to announce the result
    private void speak(String text) {
        if (tts != null && tts.getEngines().size() > 0) {  // Check if TTS engine is available
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Log.e("TTS", "Text-to-Speech not available");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        binding = null;  // Clean up binding
    }
}
