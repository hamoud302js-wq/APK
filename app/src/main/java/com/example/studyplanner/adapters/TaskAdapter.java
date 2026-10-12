package com.example.studyplanner.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.models.Task;
import com.example.studyplanner.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders tasks in a RecyclerView. Completion can be toggled inline via the checkbox.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCompletionListener {
        void onTaskCompletionChanged(Task task, boolean completed);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener clickListener;
    private final OnTaskCompletionListener completionListener;

    public TaskAdapter(OnTaskClickListener clickListener, OnTaskCompletionListener completionListener) {
        this.clickListener = clickListener;
        this.completionListener = completionListener;
    }

    public void submit(List<Task> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
        notifyDataSetChanged();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkTask;
        private final View priorityBar;
        private final TextView textTitle;
        private final TextView textDue;
        private final TextView textCategory;
        private final TextView textPriority;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTask = itemView.findViewById(R.id.checkTask);
            priorityBar = itemView.findViewById(R.id.priorityBar);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDue = itemView.findViewById(R.id.textDue);
            textCategory = itemView.findViewById(R.id.textCategory);
            textPriority = itemView.findViewById(R.id.textPriority);
        }

        void bind(Task task) {
            textTitle.setText(task.getTitle());
            textDue.setText(DateTimeUtils.dueSummary(task));
            priorityBar.setBackgroundColor(DateTimeUtils.priorityColor(task.getPriority()));
            textPriority.setText(DateTimeUtils.priorityLabel(task.getPriority()));
            textPriority.setTextColor(DateTimeUtils.priorityColor(task.getPriority()));

            String categoryName = task.getCategoryName();
            if (categoryName == null || categoryName.isEmpty()) {
                textCategory.setVisibility(View.GONE);
            } else {
                textCategory.setVisibility(View.VISIBLE);
                textCategory.setText(categoryName);
                int color = DateTimeUtils.parseColorOrDefault(task.getCategoryColor(),
                        ContextCompat.getColor(itemView.getContext(), R.color.blue_700));
                textCategory.setTextColor(color);
            }

            if (task.isCompleted()) {
                textTitle.setPaintFlags(textTitle.getPaintFlags()
                        | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(Color.GRAY);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags()
                        & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setTextColor(ContextCompat.getColor(itemView.getContext(),
                        R.color.text_primary));
            }

            checkTask.setOnCheckedChangeListener(null);
            checkTask.setChecked(task.isCompleted());
            checkTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (completionListener != null) {
                    completionListener.onTaskCompletionChanged(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });
        }
    }
}
