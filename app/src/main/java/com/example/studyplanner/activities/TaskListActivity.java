package com.example.studyplanner.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.adapters.TaskAdapter;
import com.example.studyplanner.models.Task;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * Full task list with all / pending / completed filters.
 */
public class TaskListActivity extends BaseActivity implements TaskAdapter.OnTaskClickListener,
        TaskAdapter.OnTaskCompletionListener {

    public static final String EXTRA_FILTER = "extra_filter";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private TaskAdapter adapter;
    private TextView textEmpty;
    private String currentFilter = "all";
    private long categoryId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        setupToolbar(getString(R.string.task_list_title), true);
        textEmpty = findViewById(R.id.textEmpty);

        currentFilter = getIntent().getStringExtra(EXTRA_FILTER);
        if (currentFilter == null) {
            currentFilter = "all";
        }
        categoryId = getIntent().getLongExtra(EXTRA_CATEGORY_ID, -1);

        adapter = new TaskAdapter(this, this);
        RecyclerView recycler = findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.filter_all));
        tabs.addTab(tabs.newTab().setText(R.string.filter_pending));
        tabs.addTab(tabs.newTab().setText(R.string.filter_completed));
        tabs.selectTab(tabs.getTabAt(tabIndexFor(currentFilter)));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = filterFor(tab.getPosition());
                loadTasks();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> startActivity(
                AddEditTaskActivity.newIntentForCreate(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        List<Task> tasks = categoryId > 0
                ? repository.db().getTasksByCategory(categoryId)
                : repository.getTasks(currentFilter);

        if (categoryId > 0) {
            tasks = filterLocally(tasks, currentFilter);
        }

        adapter.submit(tasks);
        textEmpty.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Task> filterLocally(List<Task> tasks, String filter) {
        if ("all".equals(filter)) {
            return tasks;
        }
        boolean wantCompleted = "completed".equals(filter);
        List<Task> result = new java.util.ArrayList<>();
        for (Task task : tasks) {
            if (task.isCompleted() == wantCompleted) {
                result.add(task);
            }
        }
        return result;
    }

    private int tabIndexFor(String filter) {
        switch (filter) {
            case "pending":
                return 1;
            case "completed":
                return 2;
            default:
                return 0;
        }
    }

    private String filterFor(int position) {
        switch (position) {
            case 1:
                return "pending";
            case 2:
                return "completed";
            default:
                return "all";
        }
    }

    @Override
    public void onTaskClick(Task task) {
        startActivity(TaskDetailsActivity.newIntent(this, task.getId()));
    }

    @Override
    public void onTaskCompletionChanged(Task task, boolean completed) {
        repository.setCompleted(task.getId(), completed);
        loadTasks();
    }
}
