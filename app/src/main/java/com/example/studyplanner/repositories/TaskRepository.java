package com.example.studyplanner.repositories;

import android.content.Context;

import com.example.studyplanner.database.TaskDbHelper;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.notifications.ReminderScheduler;

import java.util.List;

/**
 * A thin coordination layer over {@link TaskDbHelper} that keeps alarm scheduling in sync with
 * task changes. Screens depend on this rather than touching both the database and alarms directly.
 */
public class TaskRepository {

    private final Context context;
    private final TaskDbHelper db;

    public TaskRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = TaskDbHelper.getInstance(this.context);
    }

    public TaskDbHelper db() {
        return db;
    }

    public long save(Task task) {
        long id;
        if (task.getId() > 0) {
            db.updateTask(task);
            id = task.getId();
        } else {
            id = db.insertTask(task);
        }
        ReminderScheduler.cancel(context, id);
        ReminderScheduler.schedule(context, task);
        return id;
    }

    public Task getTask(long id) {
        return db.getTask(id);
    }

    public void delete(long id) {
        ReminderScheduler.cancel(context, id);
        db.deleteTask(id);
    }

    public void setCompleted(long id, boolean completed) {
        db.setTaskCompleted(id, completed);
        Task task = db.getTask(id);
        if (task != null) {
            if (completed) {
                // A completed task must not keep reminding.
                ReminderScheduler.cancel(context, id);
            } else {
                ReminderScheduler.schedule(context, task);
            }
        }
    }

    public List<Task> getTasks(String filter) {
        return db.getTasks(filter);
    }

    public List<Task> getUpcomingTasks(int limit) {
        return db.getUpcomingTasks(limit);
    }
}
