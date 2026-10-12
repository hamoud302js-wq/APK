package com.example.studyplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studyplanner.R;
import com.example.studyplanner.repositories.TaskRepository;
import com.example.studyplanner.utils.PrefsManager;

/**
 * Shared base for screens that use the common toolbar. Keeps the repository and preferences
 * setup in one place.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected TaskRepository repository;
    protected PrefsManager prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new PrefsManager(this);
        repository = new TaskRepository(this);
    }

    /** Wires the back button and title of the shared toolbar. */
    protected void setupToolbar(String title, boolean showBack) {
        TextView titleView = findViewById(R.id.toolbarTitle);
        if (titleView != null) {
            titleView.setText(title);
        }
        ImageButton back = findViewById(R.id.buttonBack);
        if (back != null) {
            back.setVisibility(showBack ? View.VISIBLE : View.INVISIBLE);
            back.setOnClickListener(v -> onBackPressed());
        }
    }

    protected void open(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }
}
