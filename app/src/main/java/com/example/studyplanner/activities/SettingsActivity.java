package com.example.studyplanner.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.studyplanner.R;
import com.example.studyplanner.utils.BackupHelper;
import com.example.studyplanner.utils.PrefsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * App preferences: notifications, sound, default reminder lead time, theme, language and a local
 * database backup/restore.
 */
public class SettingsActivity extends BaseActivity {

    private static final int[] REMINDER_MINUTES = {5, 15, 30, 60};

    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchSound;
    private Spinner spinnerReminder;
    private Spinner spinnerTheme;
    private Spinner spinnerLanguage;

    private ActivityResultLauncher<String> createBackupLauncher;
    private ActivityResultLauncher<String[]> openBackupLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar(getString(R.string.settings_title), true);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchSound = findViewById(R.id.switchSound);
        spinnerReminder = findViewById(R.id.spinnerReminder);
        spinnerTheme = findViewById(R.id.spinnerTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        setupSpinners();
        bindValues();
        registerLaunchers();

        switchNotifications.setOnCheckedChangeListener((v, checked) -> {
            prefs.setNotificationsEnabled(checked);
            if (checked) {
                requestNotificationPermission();
            }
        });
        switchSound.setOnCheckedChangeListener((v, checked) -> prefs.setSoundEnabled(checked));
        spinnerReminder.setOnItemSelectedListener(new SimpleItemSelected(position ->
                prefs.setDefaultReminderMinutes(REMINDER_MINUTES[position])));
        spinnerTheme.setOnItemSelectedListener(new SimpleItemSelected(position -> {
            prefs.setDarkMode(themeModeFor(position));
            AppCompatDelegate.setDefaultNightMode(themeModeFor(position));
        }));

        findViewById(R.id.buttonExport).setOnClickListener(v -> exportBackup());
        findViewById(R.id.buttonImport).setOnClickListener(v -> importBackup());
    }

    private void setupSpinners() {
        String[] reminderLabels = {
                getString(R.string.settings_reminder_5),
                getString(R.string.settings_reminder_15),
                getString(R.string.settings_reminder_30),
                getString(R.string.settings_reminder_60)
        };
        spinnerReminder.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, reminderLabels));

        String[] themeLabels = {
                getString(R.string.theme_light),
                getString(R.string.theme_dark),
                getString(R.string.theme_system)
        };
        spinnerTheme.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, themeLabels));

        String[] languageLabels = {
                getString(R.string.language_arabic),
                getString(R.string.language_english)
        };
        spinnerLanguage.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, languageLabels));
        spinnerLanguage.setSelection(0);
    }

    private void bindValues() {
        switchNotifications.setChecked(prefs.areNotificationsEnabled());
        switchSound.setChecked(prefs.isSoundEnabled());
        spinnerReminder.setSelection(indexOfReminder(prefs.getDefaultReminderMinutes()));
        spinnerTheme.setSelection(indexOfTheme(prefs.getDarkMode()));
    }

    private int indexOfReminder(int minutes) {
        for (int i = 0; i < REMINDER_MINUTES.length; i++) {
            if (REMINDER_MINUTES[i] == minutes) {
                return i;
            }
        }
        return 1;
    }

    private int indexOfTheme(int mode) {
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return 0;
            case AppCompatDelegate.MODE_NIGHT_YES:
                return 1;
            default:
                return 2;
        }
    }

    private int themeModeFor(int position) {
        switch (position) {
            case 0:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case 1:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    private void registerLaunchers() {
        createBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                uri -> {
                    if (uri != null) {
                        writeBackup(uri);
                    }
                });

        openBackupLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        readBackup(uri);
                    }
                });
    }

    private void exportBackup() {
        createBackupLauncher.launch("study_planner_backup.db");
    }

    private void writeBackup(Uri uri) {
        try (OutputStream output = getContentResolver().openOutputStream(uri)) {
            if (output == null) {
                throw new java.io.IOException("no output stream");
            }
            BackupHelper.export(this, output);
            Toast.makeText(this, R.string.settings_backup_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void importBackup() {
        openBackupLauncher.launch(new String[]{"*/*"});
    }

    private void readBackup(Uri uri) {
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new java.io.IOException("no input stream");
            }
            BackupHelper.restore(this, input);
            Toast.makeText(this, R.string.settings_backup_restored, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
        requestExactAlarmPermissionIfNeeded();
    }

    private void requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = getSystemService(AlarmManager.class);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                try {
                    startActivity(intent);
                } catch (Exception ignored) {
                    // Setting screen unavailable on this device; inexact alarms are used instead.
                }
            }
        }
    }

    private interface ItemSelectedListener {
        void onSelected(int position);
    }

    private static class SimpleItemSelected implements android.widget.AdapterView.OnItemSelectedListener {
        private final ItemSelectedListener listener;

        SimpleItemSelected(ItemSelectedListener listener) {
            this.listener = listener;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view,
                                   int position, long id) {
            listener.onSelected(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
