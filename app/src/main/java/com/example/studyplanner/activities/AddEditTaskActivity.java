package com.example.studyplanner.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.studyplanner.R;
import com.example.studyplanner.models.Category;
import com.example.studyplanner.models.Priority;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.utils.DateTimeUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Create or edit a task. Validates input, persists to the database and schedules the reminder.
 */
public class AddEditTaskActivity extends BaseActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private static final Priority[] PRIORITIES = Priority.values();
    private static final String[] REPEAT_KEYS = {
            Task.REPEAT_NONE, Task.REPEAT_DAILY, Task.REPEAT_WEEKLY, Task.REPEAT_MONTHLY
    };

    private TextInputLayout layoutTitle;
    private TextInputEditText inputTitle;
    private TextInputEditText inputDescription;
    private Button buttonDate;
    private Button buttonTime;
    private AutoCompleteTextView dropdownPriority;
    private AutoCompleteTextView dropdownCategory;
    private AutoCompleteTextView dropdownRepeat;
    private SwitchMaterial switchReminder;

    private Calendar dueCalendar = Calendar.getInstance();
    private List<Category> categories = new ArrayList<>();
    private long taskId = -1;
    private Task editingTask;

    private int selectedPriorityIndex = PRIORITIES.length / 2;
    private int selectedCategoryIndex = 0;
    private int selectedRepeatIndex = 0;

    public static Intent newIntentForCreate(Context context) {
        return new Intent(context, AddEditTaskActivity.class);
    }

    public static Intent newIntentForEdit(Context context, long taskId) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        boolean editing = taskId > 0;
        setupToolbar(getString(editing ? R.string.edit_task_title : R.string.add_task_title), true);

        layoutTitle = findViewById(R.id.layoutTitle);
        inputTitle = findViewById(R.id.inputTitle);
        inputDescription = findViewById(R.id.inputDescription);
        buttonDate = findViewById(R.id.buttonDate);
        buttonTime = findViewById(R.id.buttonTime);
        dropdownPriority = findViewById(R.id.dropdownPriority);
        dropdownCategory = findViewById(R.id.dropdownCategory);
        dropdownRepeat = findViewById(R.id.dropdownRepeat);
        switchReminder = findViewById(R.id.switchReminder);
        findViewById(R.id.buttonSave).setOnClickListener(v -> save());

        dueCalendar = Calendar.getInstance();
        dueCalendar.add(Calendar.HOUR_OF_DAY, 1);
        dueCalendar.set(Calendar.MINUTE, 0);
        dueCalendar.set(Calendar.SECOND, 0);
        dueCalendar.set(Calendar.MILLISECOND, 0);

        setupDropdowns();
        updateDateButton();
        updateTimeButton();
        buttonDate.setOnClickListener(v -> showDatePicker());
        buttonTime.setOnClickListener(v -> showTimePicker());

        if (editing) {
            loadTask();
        }
    }

    private void setupDropdowns() {
        categories = repository.db().getCategories();

        List<String> priorityLabels = new ArrayList<>();
        for (Priority priority : PRIORITIES) {
            priorityLabels.add(priority.getArabicLabel());
        }
        dropdownPriority.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, priorityLabels));
        dropdownPriority.setText(priorityLabels.get(selectedPriorityIndex), false);

        List<String> categoryLabels = new ArrayList<>();
        for (Category category : categories) {
            categoryLabels.add(category.getName());
        }
        dropdownCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, categoryLabels));
        if (!categoryLabels.isEmpty()) {
            dropdownCategory.setText(categoryLabels.get(selectedCategoryIndex), false);
        }

        List<String> repeatLabels = new ArrayList<>();
        for (String key : REPEAT_KEYS) {
            repeatLabels.add(DateTimeUtils.repeatLabel(key));
        }
        dropdownRepeat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, repeatLabels));
        dropdownRepeat.setText(repeatLabels.get(selectedRepeatIndex), false);

        dropdownPriority.setOnItemClickListener((parent, view, position, id) ->
                selectedPriorityIndex = position);
        dropdownCategory.setOnItemClickListener((parent, view, position, id) ->
                selectedCategoryIndex = position);
        dropdownRepeat.setOnItemClickListener((parent, view, position, id) ->
                selectedRepeatIndex = position);
    }

    private void loadTask() {
        editingTask = repository.getTask(taskId);
        if (editingTask == null) {
            Toast.makeText(this, R.string.settings_backup_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        inputTitle.setText(editingTask.getTitle());
        inputDescription.setText(editingTask.getDescription());
        dueCalendar.setTimeInMillis(editingTask.getDueDate());
        updateDateButton();
        updateTimeButton();

        selectedPriorityIndex = editingTask.getPriority().ordinal();
        dropdownPriority.setText(
                PRIORITIES[selectedPriorityIndex].getArabicLabel(), false);

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == editingTask.getCategoryId()) {
                selectedCategoryIndex = i;
                break;
            }
        }
        if (!categories.isEmpty()) {
            dropdownCategory.setText(categories.get(selectedCategoryIndex).getName(), false);
        }

        for (int i = 0; i < REPEAT_KEYS.length; i++) {
            if (REPEAT_KEYS[i].equals(editingTask.getRepeatType())) {
                selectedRepeatIndex = i;
                break;
            }
        }
        dropdownRepeat.setText(DateTimeUtils.repeatLabel(REPEAT_KEYS[selectedRepeatIndex]), false);
        switchReminder.setChecked(editingTask.isReminderEnabled());
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dueCalendar.set(Calendar.YEAR, year);
            dueCalendar.set(Calendar.MONTH, month);
            dueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateButton();
        }, dueCalendar.get(Calendar.YEAR), dueCalendar.get(Calendar.MONTH),
                dueCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            dueCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dueCalendar.set(Calendar.MINUTE, minute);
            updateTimeButton();
        }, dueCalendar.get(Calendar.HOUR_OF_DAY), dueCalendar.get(Calendar.MINUTE), false).show();
    }

    private void updateDateButton() {
        buttonDate.setText(DateTimeUtils.formatDate(dueCalendar.getTimeInMillis()));
    }

    private void updateTimeButton() {
        buttonTime.setText(DateTimeUtils.formatTime(dueCalendar.getTimeInMillis()));
    }

    private void save() {
        layoutTitle.setError(null);
        String title = inputTitle.getText() == null ? "" : inputTitle.getText().toString().trim();
        if (title.isEmpty()) {
            layoutTitle.setError(getString(R.string.error_title_required));
            return;
        }

        Task task = editingTask != null ? editingTask : new Task();
        task.setTitle(title);
        task.setDescription(inputDescription.getText() == null
                ? "" : inputDescription.getText().toString().trim());
        task.setDueDate(dueCalendar.getTimeInMillis());
        task.setPriority(PRIORITIES[selectedPriorityIndex]);
        if (!categories.isEmpty()) {
            task.setCategoryId(categories.get(selectedCategoryIndex).getId());
        }
        task.setRepeatType(REPEAT_KEYS[selectedRepeatIndex]);
        task.setReminderEnabled(switchReminder.isChecked());
        task.setReminderTime(task.getDueDate() - prefs.getDefaultReminderMinutes() * 60_000L);

        repository.save(task);

        Toast.makeText(this, editingTask != null ? R.string.task_updated : R.string.task_saved,
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
