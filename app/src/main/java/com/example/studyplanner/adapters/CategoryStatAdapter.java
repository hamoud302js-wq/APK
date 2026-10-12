package com.example.studyplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.models.Category;
import com.example.studyplanner.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the number of tasks per category with a proportional bar.
 */
public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.StatViewHolder> {

    private final List<Category> categories = new ArrayList<>();
    private final List<Integer> counts = new ArrayList<>();
    private int maxCount = 1;

    public void submit(List<Category> categories, List<Integer> counts) {
        this.categories.clear();
        this.counts.clear();
        maxCount = 1;
        if (categories != null) {
            this.categories.addAll(categories);
        }
        if (counts != null) {
            this.counts.addAll(counts);
            for (int count : counts) {
                maxCount = Math.max(maxCount, count);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_stat, parent, false);
        return new StatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        holder.bind(categories.get(position), counts.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class StatViewHolder extends RecyclerView.ViewHolder {

        private final View colorDot;
        private final TextView textName;
        private final TextView textCount;
        private final ProgressBar progress;

        StatViewHolder(@NonNull View itemView) {
            super(itemView);
            colorDot = itemView.findViewById(R.id.colorDot);
            textName = itemView.findViewById(R.id.textName);
            textCount = itemView.findViewById(R.id.textCount);
            progress = itemView.findViewById(R.id.progressCategory);
        }

        void bind(Category category, int count) {
            textName.setText(category.getName());
            textCount.setText(itemView.getContext()
                    .getString(R.string.category_tasks_count, count));
            int color = DateTimeUtils.parseColorOrDefault(category.getColor(),
                    ContextCompat.getColor(itemView.getContext(), R.color.blue_700));
            colorDot.getBackground().setTint(color);
            progress.setProgress((int) (count * 100f / maxCount));
            progress.getProgressDrawable().setTint(color);
        }
    }
}
