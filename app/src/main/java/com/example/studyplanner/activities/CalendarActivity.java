package com.example.studyplanner.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.adapters.CalendarAdapter;
import com.example.studyplanner.adapters.TaskAdapter;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Monthly calendar that highlights days with tasks and lists the selected day's tasks.
 */
public class CalendarActivity extends BaseActivity implements CalendarAdapter.OnDaySelectedListener,
        TaskAdapter.OnTaskClickListener, TaskAdapter.OnTaskCompletionListener {

    private CalendarAdapter calendarAdapter;
    private TaskAdapter dayAdapter;
    private TextView textMonth;
    private TextView textSelectedDate;
    private TextView textNoTasks;
    private RecyclerView recyclerCalendar;

    private int year;
    private int month;
    private int selectedDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setupToolbar(getString(R.string.calendar_title), true);

        textMonth = findViewById(R.id.textMonth);
        textSelectedDate = findViewById(R.id.textSelectedDate);
        textNoTasks = findViewById(R.id.textNoTasks);
        recyclerCalendar = findViewById(R.id.recyclerCalendar);

        findViewById(R.id.buttonPrevMonth).setOnClickListener(v -> changeMonth(-1));
        findViewById(R.id.buttonNextMonth).setOnClickListener(v -> changeMonth(1));

        buildWeekdayHeader();

        Calendar today = Calendar.getInstance();
        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);

        calendarAdapter = new CalendarAdapter(this);
        recyclerCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        recyclerCalendar.setAdapter(calendarAdapter);

        dayAdapter = new TaskAdapter(this, this);
        RecyclerView dayRecycler = findViewById(R.id.recyclerDayTasks);
        dayRecycler.setLayoutManager(new LinearLayoutManager(this));
        dayRecycler.setAdapter(dayAdapter);

        refreshCalendar();
        showTasksForSelectedDay();
    }

    private void buildWeekdayHeader() {
        LinearLayout header = findViewById(R.id.weekdayHeader);
        String[] weekdays = {"سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة"};
        for (String weekday : weekdays) {
            TextView text = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            text.setLayoutParams(params);
            text.setText(weekday);
            text.setGravity(Gravity.CENTER);
            text.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
            text.setTextSize(12f);
            header.addView(text);
        }
    }

    private void changeMonth(int delta) {
        month += delta;
        if (month < 0) {
            month = 11;
            year--;
        } else if (month > 11) {
            month = 0;
            year++;
        }
        selectedDay = 1;
        refreshCalendar();
        showTasksForSelectedDay();
    }

    private void refreshCalendar() {
        textMonth.setText(DateTimeUtils.formatMonth(year, month));
        calendarAdapter.setMonth(year, month, daysWithTasks(), selectedDay);
    }

    /** Builds the set of day keys (yyyyMMdd) that contain tasks in the visible month. */
    private Set<Long> daysWithTasks() {
        Calendar start = Calendar.getInstance();
        start.set(year, month, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(year, month, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);
        end.add(Calendar.MONTH, 1);

        List<Task> tasks = repository.db().getTasksBetween(
                start.getTimeInMillis(), end.getTimeInMillis() - 1);

        Set<Long> keys = new HashSet<>();
        Calendar pointer = Calendar.getInstance();
        for (Task task : tasks) {
            pointer.setTimeInMillis(task.getDueDate());
            int day = pointer.get(Calendar.DAY_OF_MONTH);
            keys.add((long) year * 10000 + (month + 1) * 100 + day);
        }
        return keys;
    }

    private void showTasksForSelectedDay() {
        Calendar start = Calendar.getInstance();
        start.set(year, month, selectedDay, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);

        textSelectedDate.setText(DateTimeUtils.formatDate(start.getTimeInMillis()));

        List<Task> tasks = repository.db().getTasksBetween(
                start.getTimeInMillis(), end.getTimeInMillis() - 1);
        dayAdapter.submit(tasks);
        textNoTasks.setVisibility(tasks.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDaySelected(int year, int month, int dayOfMonth) {
        this.selectedDay = dayOfMonth;
        calendarAdapter.setMonth(year, month, daysWithTasks(), selectedDay);
        showTasksForSelectedDay();
    }

    @Override
    public void onTaskClick(Task task) {
        startActivity(TaskDetailsActivity.newIntent(this, task.getId()));
    }

    @Override
    public void onTaskCompletionChanged(Task task, boolean completed) {
        repository.setCompleted(task.getId(), completed);
        refreshCalendar();
        showTasksForSelectedDay();
    }
}
