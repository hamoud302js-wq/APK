package com.example.studyplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.studyplanner.R;
import com.example.studyplanner.models.Category;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.utils.DateTimeUtils;

/**
 * Shows all fields of one task and exposes complete / edit / delete actions.
 */
public class TaskDetailsActivity extends BaseActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private Task task;
    private Button buttonToggleComplete;

    public static Intent newIntent(Context context, long taskId) {
        Intent intent = new Intent(context, TaskDetailsActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        setupToolbar(getString(R.string.task_details_title), true);

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        task = repository.getTask(taskId);
        if (task == null) {
            finish();
            return;
        }

        buttonToggleComplete = findViewById(R.id.buttonToggleComplete);
        buttonToggleComplete.setOnClickListener(v -> toggleComplete());
        findViewById(R.id.buttonEdit).setOnClickListener(v -> startActivity(
                AddEditTaskActivity.newIntentForEdit(this, task.getId())));
        findViewById(R.id.buttonDelete).setOnClickListener(v -> confirmDelete());

        bind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task == null) {
            return;
        }
        task = repository.getTask(task.getId());
        if (task == null) {
            finish();
            return;
        }
        bind();
    }

    private void bind() {
        ((TextView) findViewById(R.id.textTitle)).setText(task.getTitle());

        TextView description = findViewById(R.id.textDescription);
        if (task.getDescription() == null || task.getDescription().trim().isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(task.getDescription());
        }

        TextView status = findViewById(R.id.textStatus);
        status.setText(task.isCompleted() ? R.string.task_completed : R.string.task_pending);

        ((TextView) findViewById(R.id.textDue)).setText(
                getString(R.string.details_due) + ": " + DateTimeUtils.formatDateTime(task.getDueDate()));

        Category category = repository.db().getCategory(task.getCategoryId());
        String categoryName = category != null ? category.getName() : task.getCategoryName();
        ((TextView) findViewById(R.id.textCategory)).setText(
                getString(R.string.field_category) + ": "
                        + (categoryName == null ? "" : categoryName));

        ((TextView) findViewById(R.id.textPriority)).setText(
                getString(R.string.field_priority) + ": " + task.getPriority().getArabicLabel());

        ((TextView) findViewById(R.id.textRepeat)).setText(
                getString(R.string.details_repeat) + ": " + DateTimeUtils.repeatLabel(task.getRepeatType()));

        ((TextView) findViewById(R.id.textReminder)).setText(
                getString(R.string.details_reminder) + ": "
                        + getString(task.isReminderEnabled() ? R.string.reminder_on : R.string.reminder_off));

        buttonToggleComplete.setText(task.isCompleted()
                ? R.string.action_mark_incomplete : R.string.action_mark_complete);
    }

    private void toggleComplete() {
        repository.setCompleted(task.getId(), !task.isCompleted());
        task = repository.getTask(task.getId());
        if (task != null) {
            bind();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    repository.delete(task.getId());
                    Toast.makeText(this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
