package com.example.memolens.Notifications;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.memolens.TimeUtil;
import com.example.memolens.medication.Medication;

import java.util.Calendar;

public class AlarmScheduler {
    public void scheduleReminder(Context context, Medication med) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("name", med.name);
        intent.putExtra("dosage", med.dosage);
        intent.putExtra("instructions", med.instructions);
        intent.putExtra("interval", med.interval);
        //Calendar medDate = TimeUtil.addTime(TimeUtil.convertTimestampToDate(med.lastTaken), med.interval);
        Calendar medDate = Calendar.getInstance();
        medDate.add(Calendar.MINUTE, 1);
        intent.putExtra("last-taken", TimeUtil.convertDateToString(medDate));
        intent.putExtra("request-code", med.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                med.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
//        if (!medDate.before(Calendar.getInstance())) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("ALARM GOING", med.name + " " + TimeUtil.convertDateToString(medDate));
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, medDate.getTimeInMillis(), pendingIntent);
//                }
//            }
//        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, medDate.getTimeInMillis(), pendingIntent);
    }

    public void cancelReminder(Context context, Medication med) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("name", med.name);
        intent.putExtra("dosage", med.dosage);
        intent.putExtra("instructions", med.instructions);
        intent.putExtra("interval", med.interval);
        //Calendar medDate = TimeUtil.addTime(TimeUtil.convertTimestampToDate(med.lastTaken), med.interval);
        Calendar medDate = Calendar.getInstance();
        medDate.add(Calendar.MINUTE, 1);
        intent.putExtra("last-taken", TimeUtil.convertDateToString(medDate));
        intent.putExtra("request-code", med.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                med.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
//        if (!medDate.before(Calendar.getInstance())) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    Log.d("ALARM GOING", med.name + " " + TimeUtil.convertDateToString(medDate));
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, medDate.getTimeInMillis(), pendingIntent);
//                }
//            }
//        }
        alarmManager.cancel(pendingIntent);
    }

    public void replaceReminder(Context context, Medication med) {
        cancelReminder(context, med);
        scheduleReminder(context, med);
    }
}
