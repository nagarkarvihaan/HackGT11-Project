package com.example.memolens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memolens.databinding.ActivityMainBinding;
import com.example.memolens.databinding.FragmentHomeBinding;
import com.example.memolens.medication.MedicationFragment;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        LinearLayout cameraButton = binding.cameraButton;
        LinearLayout medicationButton = binding.medicationButton;

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(v.getContext(), CameraActivity.class);
//                startActivity(intent);
            }
        });

        medicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment medicationFragment = new MedicationFragment();
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container_view, medicationFragment );
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        cameraButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.softBlueFocused));
                } else {
                    v.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.softBlue));
                }
            }
        });

        medicationButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.softBlueFocused));
                } else {
                    v.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.softBlue));
                }
            }
        });

        return binding.getRoot();
    }
}
