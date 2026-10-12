package com.example.studyplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.studyplanner.models.Category;
import com.example.studyplanner.models.Priority;
import com.example.studyplanner.models.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite access layer for tasks and categories. All queries use bound parameters.
 */
public class TaskDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "study_planner.db";
    public static final int DB_VERSION = 1;

    // tasks table
    public static final String TABLE_TASKS = "tasks";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_CATEGORY_ID = "category_id";
    public static final String COL_IS_COMPLETED = "is_completed";
    public static final String COL_REMINDER_ENABLED = "reminder_enabled";
    public static final String COL_REMINDER_TIME = "reminder_time";
    public static final String COL_REPEAT_TYPE = "repeat_type";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_UPDATED_AT = "updated_at";

    // categories table
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";
    public static final String COL_CAT_COLOR = "color";
    public static final String COL_CAT_ICON = "icon";

    private static final String JOIN_CATEGORY =
            " LEFT JOIN " + TABLE_CATEGORIES + " c ON t." + COL_CATEGORY_ID
                    + " = c." + COL_CAT_ID;

    private static final String[] TASK_COLUMNS = {
            "t." + COL_ID, "t." + COL_TITLE, "t." + COL_DESCRIPTION, "t." + COL_DUE_DATE,
            "t." + COL_PRIORITY, "t." + COL_CATEGORY_ID, "t." + COL_IS_COMPLETED,
            "t." + COL_REMINDER_ENABLED, "t." + COL_REMINDER_TIME, "t." + COL_REPEAT_TYPE,
            "t." + COL_CREATED_AT, "t." + COL_UPDATED_AT,
            "c." + COL_CAT_NAME, "c." + COL_CAT_COLOR
    };

    private static volatile TaskDbHelper instance;

    public static TaskDbHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskDbHelper.class) {
                if (instance == null) {
                    instance = new TaskDbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private TaskDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CAT_NAME + " TEXT NOT NULL, "
                + COL_CAT_COLOR + " TEXT, "
                + COL_CAT_ICON + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_DUE_DATE + " INTEGER, "
                + COL_PRIORITY + " INTEGER NOT NULL DEFAULT 2, "
                + COL_CATEGORY_ID + " INTEGER, "
                + COL_IS_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_REMINDER_ENABLED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_REMINDER_TIME + " INTEGER, "
                + COL_REPEAT_TYPE + " TEXT DEFAULT 'none', "
                + COL_CREATED_AT + " INTEGER, "
                + COL_UPDATED_AT + " INTEGER, "
                + "FOREIGN KEY (" + COL_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES
                + " (" + COL_CAT_ID + "))");

        db.execSQL("CREATE INDEX idx_tasks_due ON " + TABLE_TASKS + "(" + COL_DUE_DATE + ")");
        db.execSQL("CREATE INDEX idx_tasks_category ON " + TABLE_TASKS + "(" + COL_CATEGORY_ID + ")");

        seedCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // First release: no migrations yet.
    }

    private void seedCategories(SQLiteDatabase db) {
        insertCategory(db, Category.DEFAULT_STUDY, "دراسة", "#1E88E5", "ic_cat_study");
        insertCategory(db, Category.DEFAULT_WORK, "عمل", "#43A047", "ic_cat_work");
        insertCategory(db, Category.DEFAULT_PERSONAL, "شخصية", "#8E24AA", "ic_cat_personal");
        insertCategory(db, Category.DEFAULT_HEALTH, "صحة", "#E53935", "ic_cat_health");
        insertCategory(db, Category.DEFAULT_OTHER, "أخرى", "#546E7A", "ic_cat_other");
    }

    private void insertCategory(SQLiteDatabase db, long id, String name, String color, String icon) {
        ContentValues values = new ContentValues();
        values.put(COL_CAT_ID, id);
        values.put(COL_CAT_NAME, name);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICON, icon);
        db.insert(TABLE_CATEGORIES, null, values);
    }

    // ---------------------------------------------------------------- tasks

    public long insertTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        if (task.getCreatedAt() == 0) {
            task.setCreatedAt(now);
        }
        task.setUpdatedAt(now);
        long id = db.insert(TABLE_TASKS, null, toValues(task));
        task.setId(id);
        return id;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        task.setUpdatedAt(System.currentTimeMillis());
        return db.update(TABLE_TASKS, toValues(task),
                COL_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public int deleteTask(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_TASKS, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int setTaskCompleted(long id, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_COMPLETED, completed ? 1 : 0);
        values.put(COL_UPDATED_AT, System.currentTimeMillis());
        return db.update(TABLE_TASKS, values,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, task.getTitle());
        values.put(COL_DESCRIPTION, task.getDescription());
        values.put(COL_DUE_DATE, task.getDueDate());
        values.put(COL_PRIORITY, task.getPriority().toDbValue());
        values.put(COL_CATEGORY_ID, task.getCategoryId());
        values.put(COL_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_REMINDER_ENABLED, task.isReminderEnabled() ? 1 : 0);
        values.put(COL_REMINDER_TIME, task.getReminderTime());
        values.put(COL_REPEAT_TYPE, task.getRepeatType());
        values.put(COL_CREATED_AT, task.getCreatedAt());
        values.put(COL_UPDATED_AT, task.getUpdatedAt());
        return values;
    }

    public Task getTask(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("t " + JOIN_CATEGORY, TASK_COLUMNS,
                "t." + COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    /**
     * @param filter one of {@code all}, {@code pending}, {@code completed}
     */
    public List<Task> getTasks(String filter) {
        StringBuilder selection = new StringBuilder();
        switch (filter == null ? "all" : filter) {
            case "pending":
                selection.append("t." + COL_IS_COMPLETED + " = 0");
                break;
            case "completed":
                selection.append("t." + COL_IS_COMPLETED + " = 1");
                break;
            default:
                selection.append("1 = 1");
        }
        return queryTasks(selection.toString(), null,
                "t." + COL_IS_COMPLETED + " ASC, t." + COL_DUE_DATE + " ASC, t."
                        + COL_PRIORITY + " DESC");
    }

    public List<Task> getTasksByCategory(long categoryId) {
        return queryTasks("t." + COL_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)},
                "t." + COL_DUE_DATE + " ASC");
    }

    public List<Task> searchTasks(String query, String filter, long categoryId) {
        StringBuilder selection = new StringBuilder("t." + COL_TITLE + " LIKE ?");
        List<String> args = new ArrayList<>();
        args.add("%" + query + "%");
        if ("pending".equals(filter)) {
            selection.append(" AND t." + COL_IS_COMPLETED + " = 0");
        } else if ("completed".equals(filter)) {
            selection.append(" AND t." + COL_IS_COMPLETED + " = 1");
        }
        if (categoryId > 0) {
            selection.append(" AND t." + COL_CATEGORY_ID + " = ?");
            args.add(String.valueOf(categoryId));
        }
        return queryTasks(selection.toString(), args.toArray(new String[0]),
                "t." + COL_DUE_DATE + " ASC");
    }

    public List<Task> getTasksBetween(long startMillis, long endMillis) {
        return queryTasks("t." + COL_DUE_DATE + " >= ? AND t." + COL_DUE_DATE + " <= ?",
                new String[]{String.valueOf(startMillis), String.valueOf(endMillis)},
                "t." + COL_DUE_DATE + " ASC");
    }

    public List<Task> getUpcomingTasks(int limit) {
        return queryTasks("t." + COL_IS_COMPLETED + " = 0 AND t." + COL_DUE_DATE + " >= ?",
                new String[]{String.valueOf(System.currentTimeMillis())},
                "t." + COL_DUE_DATE + " ASC LIMIT " + limit);
    }

    public List<Task> getAllTasks() {
        return queryTasks("1 = 1", null, "t." + COL_DUE_DATE + " ASC");
    }

    private List<Task> queryTasks(String selection, String[] args, String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> tasks = new ArrayList<>();
        try (Cursor cursor = db.query("t " + JOIN_CATEGORY, TASK_COLUMNS,
                selection, args, null, null, orderBy)) {
            while (cursor.moveToNext()) {
                tasks.add(fromCursor(cursor));
            }
        }
        return tasks;
    }

    private Task fromCursor(@NonNull Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        task.setDueDate(cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE)));
        task.setPriority(Priority.fromDbValue(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_PRIORITY))));
        task.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CATEGORY_ID)));
        task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1);
        task.setReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COL_REMINDER_ENABLED)) == 1);
        task.setReminderTime(cursor.getLong(cursor.getColumnIndexOrThrow(COL_REMINDER_TIME)));
        task.setRepeatType(cursor.getString(cursor.getColumnIndexOrThrow(COL_REPEAT_TYPE)));
        task.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        task.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COL_UPDATED_AT)));

        int nameIndex = cursor.getColumnIndex(COL_CAT_NAME);
        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
            task.setCategoryName(cursor.getString(nameIndex));
        }
        int colorIndex = cursor.getColumnIndex(COL_CAT_COLOR);
        if (colorIndex >= 0 && !cursor.isNull(colorIndex)) {
            task.setCategoryColor(cursor.getString(colorIndex));
        }
        return task;
    }

    // ----------------------------------------------------------- categories

    public List<Category> getCategoriesWithCounts() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT c." + COL_CAT_ID + ", c." + COL_CAT_NAME + ", c." + COL_CAT_COLOR
                + ", c." + COL_CAT_ICON + ", COUNT(t." + COL_ID + ") AS task_count"
                + " FROM " + TABLE_CATEGORIES + " c LEFT JOIN " + TABLE_TASKS + " t"
                + " ON c." + COL_CAT_ID + " = t." + COL_CATEGORY_ID
                + " GROUP BY c." + COL_CAT_ID + ", c." + COL_CAT_NAME + ", c." + COL_CAT_COLOR
                + ", c." + COL_CAT_ICON
                + " ORDER BY c." + COL_CAT_ID + " ASC";
        List<Category> categories = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                Category category = new Category(
                        cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                category.setTaskCount(cursor.getInt(4));
                categories.add(category);
            }
        }
        return categories;
    }

    public List<Category> getCategories() {
        SQLiteDatabase db = getReadableDatabase();
        List<Category> categories = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COL_CAT_ID, COL_CAT_NAME, COL_CAT_COLOR, COL_CAT_ICON},
                null, null, null, null, COL_CAT_ID + " ASC")) {
            while (cursor.moveToNext()) {
                categories.add(new Category(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3)));
            }
        }
        return categories;
    }

    public Category getCategory(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COL_CAT_ID, COL_CAT_NAME, COL_CAT_COLOR, COL_CAT_ICON},
                COL_CAT_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return new Category(cursor.getLong(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3));
            }
        }
        return null;
    }

    public int getTaskCountForCategory(long categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_CATEGORY_ID + " = ?",
                new String[]{String.valueOf(categoryId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public long insertCategory(String name, String color, String icon) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME, name);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICON, icon);
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public int updateCategory(long id, String name, String color, String icon) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_NAME, name);
        values.put(COL_CAT_COLOR, color);
        values.put(COL_CAT_ICON, icon);
        return db.update(TABLE_CATEGORIES, values,
                COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /**
     * Deletes a category. Any tasks referencing it are reassigned to {@code reassignTo} first so
     * the user never loses tasks. Pass {@code -1} to only delete when unused.
     *
     * @return true when the delete happened.
     */
    public boolean deleteCategory(long id, long reassignTo) {
        SQLiteDatabase db = getWritableDatabase();
        int taskCount = getTaskCountForCategory(id);
        if (taskCount > 0) {
            if (reassignTo <= 0) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(COL_CATEGORY_ID, reassignTo);
            db.update(TABLE_TASKS, values, COL_CATEGORY_ID + " = ?",
                    new String[]{String.valueOf(id)});
        }
        db.delete(TABLE_CATEGORIES, COL_CAT_ID + " = ?", new String[]{String.valueOf(id)});
        return true;
    }

    // ----------------------------------------------------------- statistics

    public int getTotalTaskCount() {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_TASKS);
    }

    public int getCompletedTaskCount() {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_IS_COMPLETED + " = 1");
    }

    public int getPendingTaskCount() {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_IS_COMPLETED + " = 0");
    }

    public int getTaskCountBetween(long startMillis, long endMillis) {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_DUE_DATE + " >= " + startMillis
                + " AND " + COL_DUE_DATE + " <= " + endMillis);
    }

    public int getCompletedTaskCountBetween(long startMillis, long endMillis) {
        return countQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_IS_COMPLETED + " = 1"
                + " AND " + COL_DUE_DATE + " >= " + startMillis
                + " AND " + COL_DUE_DATE + " <= " + endMillis);
    }

    private int countQuery(String sql) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }
}
