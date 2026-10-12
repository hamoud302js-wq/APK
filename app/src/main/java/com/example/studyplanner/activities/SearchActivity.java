package com.example.studyplanner.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
 * Search tasks by title with an optional status filter.
 */
public class SearchActivity extends BaseActivity implements TaskAdapter.OnTaskClickListener,
        TaskAdapter.OnTaskCompletionListener {

    private TaskAdapter adapter;
    private EditText inputSearch;
    private TextView textMessage;
    private String filter = "all";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupToolbar(getString(R.string.search_title), true);

        inputSearch = findViewById(R.id.inputSearch);
        textMessage = findViewById(R.id.textMessage);

        adapter = new TaskAdapter(this, this);
        RecyclerView recycler = findViewById(R.id.recyclerResults);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabFilter);
        tabs.addTab(tabs.newTab().setText(R.string.filter_all));
        tabs.addTab(tabs.newTab().setText(R.string.filter_pending));
        tabs.addTab(tabs.newTab().setText(R.string.filter_completed));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filter = filterFor(tab.getPosition());
                performSearch();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        findViewById(R.id.buttonSearch).setOnClickListener(v -> performSearch());
        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = inputSearch.getText().toString().trim();
        if (query.isEmpty()) {
            adapter.submit(new java.util.ArrayList<>());
            textMessage.setText(R.string.search_prompt);
            textMessage.setVisibility(View.VISIBLE);
            return;
        }
        List<Task> results = repository.db().searchTasks(query, filter, -1);
        adapter.submit(results);
        if (results.isEmpty()) {
            textMessage.setText(R.string.search_no_results);
            textMessage.setVisibility(View.VISIBLE);
        } else {
            textMessage.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
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
        performSearch();
    }
}
