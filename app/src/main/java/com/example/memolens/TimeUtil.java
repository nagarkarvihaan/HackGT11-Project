package com.example.memolens;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    static final DateFormat timeDf = new SimpleDateFormat("HH:mm");
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

    public static Calendar addTime(Calendar date, long interval) {
        date.add(Calendar.HOUR_OF_DAY, (int) interval);
        return date;
    }

    public static String getTime(String date) {
        try {
            return timeDf.format(df.parse(date));
        } catch (ParseException e){
            return date;
        }
    }

//    public static Timestamp convertStringToTimestamp() {
//        Timestamp()
//    }
}
