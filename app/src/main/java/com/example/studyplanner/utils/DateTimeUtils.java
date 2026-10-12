package com.example.studyplanner.utils;

import com.example.studyplanner.models.Priority;
import com.example.studyplanner.models.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Date and time formatting helpers that respect the Arabic locale and RTL direction.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    private static final Locale ARABIC = new Locale("ar");

    public static String formatDateTime(long millis) {
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy - hh:mm a", ARABIC);
        return format.format(new Date(millis));
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("d MMM yyyy", ARABIC).format(new Date(millis));
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a", ARABIC).format(new Date(millis));
    }

    public static String formatMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return new SimpleDateFormat("MMMM yyyy", ARABIC).format(calendar.getTime());
    }

    /** Returns a short relative label such as "اليوم" or "غداً", or null when not near. */
    public static String relativeDayLabel(long millis) {
        long today = PrefsManager.startOfToday();
        long oneDay = TimeUnit.DAYS.toMillis(1);
        if (millis >= today && millis < today + oneDay) {
            return "اليوم";
        }
        if (millis >= today + oneDay && millis < today + 2 * oneDay) {
            return "غداً";
        }
        if (millis < today) {
            return "متأخرة";
        }
        return null;
    }

    public static String priorityLabel(Priority priority) {
        return priority.getArabicLabel();
    }

    public static String repeatLabel(String repeatType) {
        if (repeatType == null) {
            return "بدون تكرار";
        }
        switch (repeatType) {
            case Task.REPEAT_DAILY:
                return "يومي";
            case Task.REPEAT_WEEKLY:
                return "أسبوعي";
            case Task.REPEAT_MONTHLY:
                return "شهري";
            default:
                return "بدون تكرار";
        }
    }

    public static int priorityColor(Priority priority) {
        switch (priority) {
            case HIGH:
                return 0xFFE53935;
            case MEDIUM:
                return 0xFFFB8C00;
            default:
                return 0xFF43A047;
        }
    }

    public static int parseColorOrDefault(String color, int fallback) {
        try {
            return android.graphics.Color.parseColor(color);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String dueSummary(Task task) {
        String relative = relativeDayLabel(task.getDueDate());
        String formatted = formatDateTime(task.getDueDate());
        if (relative != null) {
            return relative + " • " + formatted;
        }
        return formatted;
    }
}
