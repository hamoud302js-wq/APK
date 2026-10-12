package com.example.studyplanner.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;
import com.example.studyplanner.adapters.CategoryAdapter;
import com.example.studyplanner.models.Category;

import java.util.List;

/**
 * Manage categories: view counts, add, rename and delete. Deleting a category in use requires
 * choosing where its tasks go first.
 */
public class CategoriesActivity extends BaseActivity implements CategoryAdapter.CategoryActionListener {

    private static final String[] PALETTE = {
            "#1E88E5", "#43A047", "#8E24AA", "#E53935",
            "#FB8C00", "#00ACC1", "#6D4C41", "#546E7A"
    };
    private static final String[] ICONS = {
            "ic_cat_study", "ic_cat_work", "ic_cat_personal",
            "ic_cat_health", "ic_cat_other"
    };

    private CategoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        setupToolbar(getString(R.string.categories_title), true);

        adapter = new CategoryAdapter(this);
        RecyclerView recycler = findViewById(R.id.recyclerCategories);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.fabAddCategory).setOnClickListener(v -> showCategoryDialog(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        adapter.submit(repository.db().getCategoriesWithCounts());
    }

    @Override
    public void onEdit(Category category) {
        showCategoryDialog(category);
    }

    private void showCategoryDialog(@Nullable Category existing) {
        EditText input = new EditText(this);
        input.setTextDirection(android.view.View.TEXT_DIRECTION_RTL);
        input.setHint(R.string.category_name);
        if (existing != null) {
            input.setText(existing.getName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.category_add : R.string.category_edit)
                .setView(input)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = input.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        input.setError(getString(R.string.category_name));
                        return;
                    }
                    if (existing == null) {
                        int index = repository.db().getCategories().size() % PALETTE.length;
                        repository.db().insertCategory(name, PALETTE[index],
                                ICONS[index % ICONS.length]);
                    } else {
                        String color = existing.getColor() == null ? PALETTE[0] : existing.getColor();
                        String icon = existing.getIcon() == null ? ICONS[0] : existing.getIcon();
                        repository.db().updateCategory(existing.getId(), name, color, icon);
                    }
                    Toast.makeText(this, R.string.category_saved, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    load();
                }));
        dialog.show();
    }

    @Override
    public void onDelete(Category category) {
        int count = repository.db().getTaskCountForCategory(category.getId());
        if (count == 0) {
            repository.db().deleteCategory(category.getId(), -1);
            Toast.makeText(this, R.string.category_deleted, Toast.LENGTH_SHORT).show();
            load();
            return;
        }
        promptReassign(category);
    }

    /** Asks where to move the tasks of a category that is about to be deleted. */
    private void promptReassign(Category category) {
        List<Category> others = repository.db().getCategories();
        others.removeIf(c -> c.getId() == category.getId());
        if (others.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.category_delete_in_use_title)
                    .setMessage(R.string.category_delete_in_use_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        String[] names = new String[others.size()];
        for (int i = 0; i < others.size(); i++) {
            names[i] = others.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.category_delete_in_use_title)
                .setMessage(getString(R.string.category_reassign_to))
                .setItems(names, (dialog, which) -> {
                    repository.db().deleteCategory(category.getId(), others.get(which).getId());
                    Toast.makeText(this, R.string.category_deleted, Toast.LENGTH_SHORT).show();
                    load();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
