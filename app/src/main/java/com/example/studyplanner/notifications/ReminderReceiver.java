package com.example.studyplanner.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.studyplanner.R;
import com.example.studyplanner.activities.TaskDetailsActivity;
import com.example.studyplanner.database.TaskDbHelper;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.utils.DateTimeUtils;
import com.example.studyplanner.utils.PrefsManager;

/**
 * Receives scheduled reminder alarms and posts a notification that opens the task details.
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_REMINDER = "com.example.studyplanner.ACTION_REMINDER";
    public static final String CHANNEL_ID = "study_planner_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, -1);
        if (taskId <= 0) {
            return;
        }

        TaskDbHelper db = TaskDbHelper.getInstance(context);
        Task task = db.getTask(taskId);
        if (task == null || task.isCompleted()) {
            return;
        }

        PrefsManager prefs = new PrefsManager(context);
        if (!prefs.areNotificationsEnabled()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannel(context);

        Intent detailsIntent = TaskDetailsActivity.newIntent(context, taskId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) taskId, detailsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(task.getTitle())
                .setContentText("موعد الاستحقاق: " + DateTimeUtils.formatDateTime(task.getDueDate()))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(task.getDescription() == null || task.getDescription().trim().isEmpty()
                                ? "لا يوجد وصف"
                                : task.getDescription()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        if (prefs.isSoundEnabled()) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            // Task id is the notification id, so the same task never produces duplicates.
            manager.notify((int) taskId, builder.build());
        }
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "تذكيرات المهام",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("إشعارات المواعيد النهائية للمهام");
        manager.createNotificationChannel(channel);
    }
}
