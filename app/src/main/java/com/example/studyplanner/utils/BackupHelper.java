package com.example.studyplanner.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.studyplanner.database.TaskDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Exports and restores the SQLite database file for local backup.
 */
public final class BackupHelper {

    private BackupHelper() {
    }

    /** Copies the current database to the given output stream. */
    public static void export(Context context, OutputStream outputStream) throws IOException {
        SQLiteDatabase db = TaskDbHelper.getInstance(context).getWritableDatabase();
        // Flush pending writes to disk before copying the file.
        db.close();

        File dbFile = context.getDatabasePath(TaskDbHelper.DB_NAME);
        try (InputStream input = new FileInputStream(dbFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
        outputStream.flush();
    }

    /** Replaces the current database with the contents of the given input stream. */
    public static void restore(Context context, InputStream inputStream) throws IOException {
        // Close any open handle so the file can be replaced safely.
        TaskDbHelper.getInstance(context).close();

        File dbFile = context.getDatabasePath(TaskDbHelper.DB_NAME);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (OutputStream output = new FileOutputStream(dbFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        }
    }
}
