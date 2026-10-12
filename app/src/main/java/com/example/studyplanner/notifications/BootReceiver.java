package com.example.studyplanner.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.studyplanner.database.TaskDbHelper;
import com.example.studyplanner.models.Task;

import java.util.List;

/**
 * Re-schedules all pending reminders after the device reboots, since alarms do not survive a
 * restart.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        TaskDbHelper db = TaskDbHelper.getInstance(context);
        List<Task> tasks = db.getAllTasks();
        for (Task task : tasks) {
            if (task.isReminderEnabled() && !task.isCompleted()
                    && task.getReminderTime() > System.currentTimeMillis()) {
                ReminderScheduler.schedule(context, task);
            }
        }
    }
}
