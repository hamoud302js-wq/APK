package com.example.studyplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyplanner.R;
import com.example.studyplanner.notifications.ReminderReceiver;
import com.example.studyplanner.utils.PrefsManager;

/**
 * Shows the logo briefly, then routes to the login or dashboard screen.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefsManager prefs = new PrefsManager(this);
        prefs.applyTheme();
        setContentView(R.layout.activity_splash);

        ReminderReceiver.createChannel(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        if (isFinishing()) {
            return;
        }
        PrefsManager prefs = new PrefsManager(this);
        Intent next = prefs.isLoggedIn()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginActivity.class);
        startActivity(next);
        finish();
    }
}
