package com.example.studyplanner.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.studyplanner.models.Task;

/**
 * Schedules and cancels exact reminders for tasks. Uses the task id as the request code so a
 * reminder can always be replaced or cancelled by id.
 */
public final class ReminderScheduler {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DUE = "extra_due";

    private ReminderScheduler() {
    }

    /**
     * Schedules a reminder for the task. If reminders are disabled or the reminder time is in the
     * past, any existing reminder is cancelled instead.
     */
    public static void schedule(Context context, Task task) {
        if (!task.isReminderEnabled() || task.isCompleted()) {
            cancel(context, task.getId());
            return;
        }
        long triggerAt = task.getReminderTime();
        if (triggerAt <= 0) {
            triggerAt = task.getDueDate();
        }
        if (triggerAt <= System.currentTimeMillis()) {
            cancel(context, task.getId());
            return;
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(context, task, PendingIntent.FLAG_UPDATE_CURRENT);

        if (canScheduleExact(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            // Fall back to an inexact alarm when the exact-alarm permission is unavailable,
            // typically on Android 12+ without user consent.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }

    public static void cancel(Context context, long taskId) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = buildPendingIntent(context, taskId, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static boolean canScheduleExact(AlarmManager alarmManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    private static PendingIntent buildPendingIntent(Context context, Task task, int flag) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_REMINDER + "." + task.getId());
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        intent.putExtra(EXTRA_TITLE, task.getTitle());
        intent.putExtra(EXTRA_DUE, task.getDueDate());
        return PendingIntent.getBroadcast(context, (int) task.getId(), intent,
                flag | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent buildPendingIntent(Context context, long taskId, int flag) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(ReminderReceiver.ACTION_REMINDER + "." + taskId);
        return PendingIntent.getBroadcast(context, (int) taskId, intent,
                flag | PendingIntent.FLAG_IMMUTABLE);
    }
}
