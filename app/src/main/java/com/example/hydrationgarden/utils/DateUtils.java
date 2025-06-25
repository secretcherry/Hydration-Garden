package com.example.hydrationgarden.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }

    public static String formatDateForDisplay(Date date) {
        return DISPLAY_DATE_FORMAT.format(date);
    }

    public static String formatTimeForDisplay(Date date) {
        return TIME_FORMAT.format(date);
    }

    public static String getDateString(Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static boolean isToday(Date date) {
        return getDateString(date).equals(getCurrentDate());
    }

    public static Date[] getLast7Days() {
        Date[] dates = new Date[7];
        Calendar calendar = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            dates[6-i] = calendar.getTime();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        return dates;
    }

    public static String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Dobro jutro";
        } else if (hour < 17) {
            return "Dobar dan";
        } else {
            return "Dobra večer";
        }
    }
}