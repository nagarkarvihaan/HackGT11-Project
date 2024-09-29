package com.example.memolens;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memolens.databinding.ActivityMainBinding;
import com.example.memolens.medication.MedicationFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    NotificationManager notificationManager;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        createNotificationChannel();
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("fragment")) {
            Bundle bundle = new Bundle();
            bundle.putString("med-name", getIntent().getStringExtra("med-name"));
            Fragment medicationFragment = new MedicationFragment();
            medicationFragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_view, medicationFragment );
            transaction.addToBackStack(null);
            transaction.commit();
        }
        //checkExactAlarmPermission();
        //askBatteryOptimizations();
        setContentView(binding.getRoot());
    }

    // Method to check for permission and request it if not granted
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    != PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs permission to set exact alarms for timely medication reminders.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            // User clicked Allow, open settings to request permission
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // User clicked Cancel
                            dialog.dismiss();
                            Toast.makeText(this, "Permission denied. Alarms may not function as expected.", Toast.LENGTH_LONG).show();
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void askBatteryOptimizations() {
        new AlertDialog.Builder(this)
                .setTitle("Battery Optimization Warning")
                .setMessage("To ensure that your medication reminders are delivered on time, please exclude our app from battery optimization. This will prevent the system from blocking alarms while your device is idle.")
                .setPositiveButton("Exclude Now", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void createNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_CODE_POST_NOTIFICATIONS);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(
                            "medication_reminder_channel",
                            "Medication Reminder",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    notificationManager.createNotificationChannel(channel);
                }
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied for notifications", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
