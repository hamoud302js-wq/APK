package com.example.studyplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyplanner.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Grid adapter for the monthly calendar. Days outside the current month are blank; days with
 * tasks show a dot, and the selected day is highlighted.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    public interface OnDaySelectedListener {
        void onDaySelected(int year, int month, int dayOfMonth);
    }

    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_DAY = 1;

    private final List<Integer> days = new ArrayList<>();
    private final OnDaySelectedListener listener;
    private final java.util.Set<Long> daysWithTasks = new java.util.HashSet<>();

    private int year;
    private int month;
    private int selectedDay = -1;

    public CalendarAdapter(OnDaySelectedListener listener) {
        this.listener = listener;
    }

    public void setMonth(int year, int month, java.util.Set<Long> daysWithTasks, int selectedDay) {
        this.year = year;
        this.month = month;
        this.selectedDay = selectedDay;
        this.daysWithTasks.clear();
        if (daysWithTasks != null) {
            this.daysWithTasks.addAll(daysWithTasks);
        }
        buildDays();
        notifyDataSetChanged();
    }

    private void buildDays() {
        days.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // In RTL the grid starts on Saturday; align blanks so the 1st lands on the right column.
        int leadingBlanks = (firstDayOfWeek - Calendar.SATURDAY + 7) % 7;
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(TYPE_EMPTY);
        }
        for (int day = 1; day <= maxDay; day++) {
            days.add(day);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return days.get(position) == TYPE_EMPTY ? TYPE_EMPTY : TYPE_DAY;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {

        private final TextView textDay;
        private final View dotIndicator;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.textDay);
            dotIndicator = itemView.findViewById(R.id.dotIndicator);
        }

        void bind(int day) {
            if (day == TYPE_EMPTY) {
                textDay.setText("");
                dotIndicator.setVisibility(View.INVISIBLE);
                itemView.setOnClickListener(null);
                return;
            }
            textDay.setText(String.valueOf(day));

            if (day == selectedDay) {
                textDay.setBackgroundResource(R.drawable.bg_circle_selected);
                textDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                textDay.setBackground(null);
                textDay.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_primary));
            }

            boolean hasTasks = daysWithTasks.contains(dayKey(day));
            dotIndicator.setVisibility(hasTasks ? View.VISIBLE : View.INVISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDaySelected(year, month, day);
                }
            });
        }
    }

    private long dayKey(int day) {
        return (long) year * 10000 + (month + 1) * 100 + day;
    }
}
