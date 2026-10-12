package com.example.studyplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.models.Category;
import com.example.studyplanner.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays categories with their task counts, plus inline edit and delete actions.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface CategoryActionListener {
        void onEdit(Category category);

        void onDelete(Category category);
    }

    private final List<Category> categories = new ArrayList<>();
    private final CategoryActionListener listener;

    public CategoryAdapter(CategoryActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Category> newCategories) {
        categories.clear();
        if (newCategories != null) {
            categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageIcon;
        private final TextView textName;
        private final TextView textCount;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            textName = itemView.findViewById(R.id.textName);
            textCount = itemView.findViewById(R.id.textCount);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(Category category) {
            textName.setText(category.getName());
            textCount.setText(itemView.getContext()
                    .getString(R.string.category_tasks_count, category.getTaskCount()));

            int color = DateTimeUtils.parseColorOrDefault(category.getColor(),
                    ContextCompat.getColor(itemView.getContext(), R.color.blue_700));
            imageIcon.setImageResource(iconFor(category.getIcon()));
            imageIcon.setColorFilter(color);

            buttonEdit.setOnClickListener(v -> listener.onEdit(category));
            buttonDelete.setOnClickListener(v -> listener.onDelete(category));
        }

        @DrawableRes
        private int iconFor(String icon) {
            if (icon == null) {
                return R.drawable.ic_cat_other;
            }
            switch (icon) {
                case "ic_cat_study":
                    return R.drawable.ic_cat_study;
                case "ic_cat_work":
                    return R.drawable.ic_cat_work;
                case "ic_cat_personal":
                    return R.drawable.ic_cat_personal;
                case "ic_cat_health":
                    return R.drawable.ic_cat_health;
                default:
                    return R.drawable.ic_cat_other;
            }
        }
    }
}
