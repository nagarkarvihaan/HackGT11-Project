package com.example.memolens.medication;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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

import org.checkerframework.checker.units.qual.A;

public class MedicationFragment extends Fragment {
    final String COLLECTION_PATH = "Medications";
    final DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm");

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
        addMedication.setOnClickListener(v -> showDialog(true));

        medicationCount = binding.medicationCount;
        curMedication = binding.currentMedication;
        curMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(false);
            }
        });

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



    public void showDialog(boolean adding) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View promptView = layoutInflater.inflate(R.layout.medication_dialog, null);

        final AlertDialog alertD = new AlertDialog.Builder(getContext()).create();
        final AtomicBoolean isDateSet = new AtomicBoolean(!adding);
        EditText lastTakenView = ((TextInputLayout) promptView.findViewById(R.id.lastTaken)).getEditText();
        Calendar date = Calendar.getInstance();
        lastTakenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar currentDate = Calendar.getInstance();
                DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        isDateSet.set(true);
                        date.set(year, monthOfYear, dayOfMonth);
                        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                date.set(Calendar.MINUTE, minute);
                                lastTakenView.setText(TimeUtil.convertDateToString(date));
                            }
                        }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
                    }
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
                dpd.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                dpd.show();
            }
        });

        Button saveButton = (Button) promptView.findViewById(R.id.saveMedication);
        Button deleteButton = (Button) promptView.findViewById(R.id.deleteMedication);
        Button backButton = (Button) promptView.findViewById(R.id.backButton);
        if (adding) {
            deleteButton.setVisibility(View.INVISIBLE);
            deleteButton.setClickable(false);
        } else {
            fillInFields(promptView);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Medication med = validateMedication(promptView, date, isDateSet);
                if (med != null) {
                    String name = getEditTextValue(promptView.findViewById(R.id.name));
                    setMedication(med, name, adding);
                    alertD.dismiss();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                confirmAndDelete(alertD);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertD.dismiss();
            }
        });

        alertD.setView(promptView);
        alertD.show();
    }

    public void confirmAndDelete(AlertDialog alertD) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Confirm deletion of the medicine");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteMedication();
                dialog.dismiss();
                alertD.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteMedication() {
        Medication currentMedication = medicationList.get(cur);
        db.collection(COLLECTION_PATH).document(currentMedication.name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        medicationList.remove(cur);
                        displayMedication(-1);
                        Toast.makeText(getContext(), currentMedication.name + " deleted from list", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), currentMedication.name + "could not be deleted from list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fillInFields(View promptView) {
        EditText nameView = ((TextInputLayout) promptView.findViewById(R.id.name)).getEditText();
        EditText dosageView = ((TextInputLayout) promptView.findViewById(R.id.dosage)).getEditText();
        EditText instructionsView = ((TextInputLayout) promptView.findViewById(R.id.instructions)).getEditText();
        EditText intervalStrView = ((TextInputLayout) promptView.findViewById(R.id.interval)).getEditText();
        EditText lastTakenView = ((TextInputLayout) promptView.findViewById(R.id.lastTaken)).getEditText();

        Medication currentMedication = medicationList.get(cur);
        nameView.setText(currentMedication.name);
        dosageView.setText(currentMedication.dosage);
        instructionsView.setText(currentMedication.instructions);
        intervalStrView.setText(String.valueOf(currentMedication.interval));
        lastTakenView.setText(TimeUtil.convertDateToString(TimeUtil.convertTimestampToDate(currentMedication.lastTaken)));
    }

    public Medication validateMedication(View promptView, Calendar date, AtomicBoolean isDateSet) {
        boolean valid = true;
        String name = getEditTextValue(promptView.findViewById(R.id.name));
        if (name.isBlank()) {
            valid = false;
            setError(promptView.findViewById(R.id.name), "Name cannot be empty");
        }
        String dosage = getEditTextValue(promptView.findViewById(R.id.dosage));
        if (dosage.isBlank()) {
            valid = false;
            setError(promptView.findViewById(R.id.dosage), "Dosage cannot be empty");
        }
        String instructions = getEditTextValue(promptView.findViewById(R.id.instructions));
        String intervalStr = getEditTextValue(promptView.findViewById(R.id.interval));
        try {
            if (intervalStr.isBlank()) {
                throw new Exception();
            }
            Long.parseLong(intervalStr);
        } catch (Exception e) {
            valid = false;
            setError(promptView.findViewById(R.id.interval), "Interval must be an integer number of hours");
        }
        if (date == null) {
            valid = false;
            setError(promptView.findViewById(R.id.lastTaken), "Date must be set");
        }
        if (!isDateSet.get()) {
            valid = false;
            setError(promptView.findViewById(R.id.lastTaken), "Date cannot be blank");
        }
        if (date.compareTo(Calendar.getInstance()) > 0) {
            valid = false;
            setError(promptView.findViewById(R.id.lastTaken), "Date cannot be in the future");
        }
        if (!valid) {
            return null;
        }
        Medication med = Medication.createMedication(name, dosage, instructions, Long.parseLong(intervalStr),
                TimeUtil.convertDateToTimestamp(date));
        return med;
    }

    public void setError(View v, String errorMessage) {
        ((TextInputLayout) v).setError(errorMessage);
    }

    public void setMedication(Medication med, String documentName, boolean adding) {
        db.collection(COLLECTION_PATH).document(documentName)
                .set(med.getMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (adding) {
                            medicationList.add(cur, med);
                        } else {
                            medicationList.set(cur, med);
                        }
                        displayMedication(0);
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
