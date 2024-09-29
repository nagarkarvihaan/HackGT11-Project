package com.example.memolens.Notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.util.Log;

import com.example.memolens.MainActivity;
import com.example.memolens.R;
import com.example.memolens.TimeUtil;

public class ReminderReceiver extends BroadcastReceiver {
    final String NOTIF_GROUP = "com.example.memolens.NOTIFICATIONS";
    String name, instructions, dosage, lastTaken;
    long interval;
    int requestCode;
    @Override
    public void onReceive(Context context, Intent intent) {
        name = intent.getStringExtra("name");
        dosage = intent.getStringExtra("dosage");
        instructions = intent.getStringExtra("instructions");
        interval = intent.getLongExtra("interval", 1);
        lastTaken = intent.getStringExtra("last-taken");
        requestCode = intent.getIntExtra("request-code", 0);
        showNotification(context);
    }

    private void showNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "MedicationFragment");  // Pass the fragment identifier
        intent.putExtra("med-name", name);
        Log.d("SENDING", name);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message = String.format("It's %s. Time to take %s!", TimeUtil.getTime(lastTaken), name);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medication_reminder_channel")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Medication Reminder")
                .setContentText(message)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setGroup(NOTIF_GROUP)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(requestCode, builder.build());
        }
    }
}
