package com.example.memolens.medication;

import java.util.*;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memolens.HomeFragment;
import com.example.memolens.R;
import com.example.memolens.databinding.FragmentMedicationBinding;
import com.example.memolens.firebase.FirestoreCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MedicationFragment extends Fragment {

    FragmentMedicationBinding binding;
    FirebaseFirestore db;
    ImageButton next;
    ImageButton back;
    ImageButton addMedication;
    ImageButton backMain;
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
        medicationCount = binding.medicationCount;

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

    public void displayMedication(int offset) {
        cur = (cur + offset + medicationList.size()) % medicationList.size();
        Medication currentMedication = medicationList.get(cur);

        //todo: Update each field in table view
        binding.dosageText.setText("Every" + currentMedication.dosage + " hours");
        binding.lastTimeTakenText.setText(currentMedication.lastTaken.toString());
        binding.medicationTitle.setText(currentMedication.name);
        if (currentMedication.instructions != "") {
            binding.instructionsText.setText(currentMedication.instructions);
        } else {
            binding.instructionsText.setText("No instructions listed.");
        }

        setMedicationCount();
    }

    public void setMedicationCount() {
        String countStr = (cur + 1) + " of " + medicationList.size();
        medicationCount.setText(countStr);
    }

    public void readMedications(final FirestoreCallback firestoreCallback) {
        List<DocumentSnapshot> documents = null;
        db.collection("Medications")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            firestoreCallback.onCallbackDocuments(task.getResult().getDocuments());
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("SOMETHING", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("SOMETHING", "Error getting documents.", task.getException());
                        }
                    }
                });
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
