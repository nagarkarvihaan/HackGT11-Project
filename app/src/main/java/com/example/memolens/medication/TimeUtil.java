package com.example.memolens.medication;

import com.google.firebase.Timestamp;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    public static Calendar convertTimestampToDate(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getSeconds() * 1000);
        return calendar;
    }

    public static String convertDateToString(Calendar date) {
        return df.format(date.getTime());
    }

    public static Timestamp convertDateToTimestamp(Calendar date) {
        return new Timestamp(date.getTimeInMillis() / 1000, 0);
    }
}
