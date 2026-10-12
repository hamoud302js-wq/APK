package com.example.studyplanner.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.adapters.CategoryStatAdapter;
import com.example.studyplanner.database.TaskDbHelper;
import com.example.studyplanner.models.Category;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Completion overview. All figures are computed from the database for the selected period.
 */
public class StatisticsActivity extends BaseActivity {

    private CategoryStatAdapter adapter;
    private TextView textTotal;
    private TextView textCompleted;
    private TextView textPending;
    private TextView textRate;
    /** 0 = all time, 1 = this week, 2 = this month. */
    private int period = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setupToolbar(getString(R.string.statistics_title), true);

        textTotal = findViewById(R.id.textTotal);
        textCompleted = findViewById(R.id.textCompleted);
        textPending = findViewById(R.id.textPending);
        textRate = findViewById(R.id.textCompletionRate);

        adapter = new CategoryStatAdapter();
        RecyclerView recycler = findViewById(R.id.recyclerCategoryStats);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        TabLayout tabs = findViewById(R.id.tabPeriod);
        tabs.addTab(tabs.newTab().setText(R.string.period_all));
        tabs.addTab(tabs.newTab().setText(R.string.period_week));
        tabs.addTab(tabs.newTab().setText(R.string.period_month));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                period = tab.getPosition();
                refresh();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        TaskDbHelper db = repository.db();
        int total;
        int completed;
        if (period == 0) {
            total = db.getTotalTaskCount();
            completed = db.getCompletedTaskCount();
        } else {
            long start = startOfPeriod();
            long end = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000;
            total = db.getTaskCountBetween(start, end);
            completed = db.getCompletedTaskCountBetween(start, end);
        }
        int pending = total - completed;
        int rate = total == 0 ? 0 : Math.round(completed * 100f / total);

        textTotal.setText(String.valueOf(total));
        textCompleted.setText(String.valueOf(completed));
        textPending.setText(String.valueOf(pending));
        textRate.setText(rate + "%");

        loadCategoryBreakdown();
    }

    private long startOfPeriod() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (period == 2) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            // Start of the week (Saturday for the Arabic calendar).
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int diff = (dayOfWeek - Calendar.SATURDAY + 7) % 7;
            calendar.add(Calendar.DAY_OF_MONTH, -diff);
        }
        return calendar.getTimeInMillis();
    }

    private void loadCategoryBreakdown() {
        List<Category> categories = repository.db().getCategoriesWithCounts();
        List<Integer> counts = new ArrayList<>();
        for (Category category : categories) {
            counts.add(category.getTaskCount());
        }
        adapter.submit(categories, counts);
    }
}
