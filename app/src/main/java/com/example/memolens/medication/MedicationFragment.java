package com.example.memolens.medication;

import java.util.*;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memolens.HomeFragment;
import com.example.memolens.R;
import com.example.memolens.databinding.FragmentMedicationBinding;
import com.example.memolens.firebase.FirestoreCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MedicationFragment extends Fragment {
    final String COLLECTION_PATH = "Medications";
    FragmentMedicationBinding binding;
    FirebaseFirestore db;
    Button next, back, addMedication, backMain;
    TextView medicationCount, curMedication;
    int cur = 0;
    List<Medication> medicationList = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicationBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();

        next = binding.nextButton;
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMedication(1);
            }
        });
        back = binding.prevButton;
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMedication(-1);
            }
        });

        readMedications(new FirestoreCallback() {
            @Override
            public void onCallbackDocuments(List<DocumentSnapshot> documents) {
                for (DocumentSnapshot document : documents) {
                    medicationList.add(new Medication(document.getData()));
                }
                displayMedication(0);
            }
        });

        addMedication = binding.addMedication;
        addMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
//                Medication newMed = addNewMedication();
                //medicationList.add(cur, newMed);
                //displayMedication(0);
            }
        });

        medicationCount = binding.medicationCount;
        curMedication = binding.currentMedication;

        backMain = binding.backMain;
        backMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment homeFragment = new HomeFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_view, homeFragment );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return binding.getRoot();
    }

    public Medication createMedication(String name, String dosage, String instructions,
                                       String interval, String lastTaken) {
        return medicationList.get(0);
    }

    public void showDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.medication_dialog, null);

        final AlertDialog alertD = new AlertDialog.Builder(getContext()).create();

        Button saveButton = (Button) promptView.findViewById(R.id.saveMedication);
        Button deleteButton = (Button) promptView.findViewById(R.id.deleteMedication);

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = getEditTextValue(promptView.findViewById(R.id.name));
                String dosage = getEditTextValue(promptView.findViewById(R.id.dosage));
                String instructions = getEditTextValue(promptView.findViewById(R.id.instructions));
                String interval = getEditTextValue(promptView.findViewById(R.id.interval));
                String lastTaken = getEditTextValue(promptView.findViewById(R.id.lastTaken));

                Log.d("HERE", name + " " + dosage + " " + instructions + " " + interval + " " + lastTaken);
                Medication med = createMedication(name, dosage, instructions, interval, lastTaken);
                setMedication(med, name);
                alertD.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        alertD.setView(promptView);
        alertD.show();
    }

    public void setMedication(Medication med, String documentName) {
        Log.d("NAME", documentName);
        db.collection(COLLECTION_PATH).document(documentName)
                .set(med.getMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Medication updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error retrieving medications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void displayMedication(int offset) {
        if (medicationList.size() == 0) {
            curMedication.setText("No medications");
            medicationCount.setText("0 of 0");
            return;
        }
        cur = (cur + offset + medicationList.size()) % medicationList.size();
        curMedication.setText(medicationList.get(cur).name);
        setMedicationCount();
    }

    public void setMedicationCount() {
        String countStr = (cur + 1) + " of " + medicationList.size();
        medicationCount.setText(countStr);
    }

    public void readMedications(final FirestoreCallback firestoreCallback) {
        List<DocumentSnapshot> documents = null;
        db.collection(COLLECTION_PATH)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            firestoreCallback.onCallbackDocuments(task.getResult().getDocuments());
                        } else {
                            Toast.makeText(getContext(), "Error retrieving medications", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public String getEditTextValue(View v) {
        return ((TextInputLayout) v).getEditText().getText().toString();
    }

    public void onStop() {
        super.onStop();
        cur = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cur = 0;
    }
}
