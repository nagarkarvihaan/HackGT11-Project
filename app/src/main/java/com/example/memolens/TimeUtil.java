package com.example.memolens;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    public static Timestamp convertMilitaryStringToTimestamp(String dateStr) {
        Calendar cal = Calendar.getInstance();
        DateFormat militaryDf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date curDate = militaryDf.parse(dateStr.substring(0, 10));
            Date curTime = timeDf.parse(dateStr.substring(11, 16));

            cal.setTime(curDate);

            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(curTime);

            cal.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
        } catch (ParseException e) {
        }
        return TimeUtil.convertDateToTimestamp(cal);
    }

    public static String convertTimestampToMilitaryString(Timestamp ts) {
        Calendar cal = TimeUtil.convertTimestampToDate(ts);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        return sdf.format(cal.getTime());
    }
}
