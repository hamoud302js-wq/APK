package com.example.studyplanner.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.adapters.TaskAdapter;
import com.example.studyplanner.database.TaskDbHelper;
import com.example.studyplanner.models.Task;

import java.util.List;

/**
 * Dashboard: summary cards plus a short list of the next tasks.
 */
public class MainActivity extends BaseActivity implements TaskAdapter.OnTaskClickListener,
        TaskAdapter.OnTaskCompletionListener {

    private static final int UPCOMING_LIMIT = 5;

    private TextView textTotal;
    private TextView textCompleted;
    private TextView textPending;
    private TextView textCompletionRate;
    private TextView textNoUpcoming;
    private ProgressBar progressCompletion;
    private TaskAdapter adapter;
    private TaskDbHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = repository.db();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        textTotal = findViewById(R.id.textTotal);
        textCompleted = findViewById(R.id.textCompleted);
        textPending = findViewById(R.id.textPending);
        textCompletionRate = findViewById(R.id.textCompletionRate);
        textNoUpcoming = findViewById(R.id.textNoUpcoming);
        progressCompletion = findViewById(R.id.progressCompletion);
        findViewById(R.id.textUserName).setVisibility(View.VISIBLE);

        adapter = new TaskAdapter(this, this);
        RecyclerView recycler = findViewById(R.id.recyclerUpcoming);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.buttonAddTask).setOnClickListener(v -> startActivity(
                AddEditTaskActivity.newIntentForCreate(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        int total = db.getTotalTaskCount();
        int completed = db.getCompletedTaskCount();
        int pending = total - completed;
        int rate = total == 0 ? 0 : Math.round(completed * 100f / total);

        textTotal.setText(String.valueOf(total));
        textCompleted.setText(String.valueOf(completed));
        textPending.setText(String.valueOf(pending));
        textCompletionRate.setText(rate + "%");
        progressCompletion.setProgress(rate);

        String name = prefs.getUserName();
        TextView userName = findViewById(R.id.textUserName);
        if (name != null && !name.isEmpty()) {
            userName.setText(name);
            userName.setVisibility(View.VISIBLE);
        } else {
            userName.setVisibility(View.GONE);
        }

        List<Task> upcoming = db.getUpcomingTasks(UPCOMING_LIMIT);
        adapter.submit(upcoming);
        textNoUpcoming.setVisibility(upcoming.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            open(SearchActivity.class);
            return true;
        } else if (id == R.id.action_calendar) {
            open(CalendarActivity.class);
            return true;
        } else if (id == R.id.action_statistics) {
            open(StatisticsActivity.class);
            return true;
        } else if (id == R.id.action_settings) {
            open(SettingsActivity.class);
            return true;
        } else if (id == R.id.action_categories) {
            open(CategoriesActivity.class);
            return true;
        } else if (id == R.id.action_all_tasks) {
            open(TaskListActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskClick(Task task) {
        startActivity(TaskDetailsActivity.newIntent(this, task.getId()));
    }

    @Override
    public void onTaskCompletionChanged(Task task, boolean completed) {
        repository.setCompleted(task.getId(), completed);
        refresh();
    }
}
