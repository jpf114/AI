package com.example.timedisplay.health.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timedisplay.R;
import com.example.timedisplay.health.model.ExerciseRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExerciseRecordAdapter extends RecyclerView.Adapter<ExerciseRecordAdapter.ViewHolder> {

    private List<ExerciseRecord> records = new ArrayList<>();
    private OnItemClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(ExerciseRecord record);
        void onItemLongClick(ExerciseRecord record);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<ExerciseRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExerciseType;
        private TextView tvIntensity;
        private TextView tvTime;
        private TextView tvDuration;
        private TextView tvCalories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseType = itemView.findViewById(R.id.tvExerciseType);
            tvIntensity = itemView.findViewById(R.id.tvIntensity);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCalories = itemView.findViewById(R.id.tvCalories);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(records.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(records.get(position));
                    return true;
                }
                return false;
            });
        }

        void bind(ExerciseRecord record) {
            tvExerciseType.setText(record.getExerciseTypeDisplay());
            tvIntensity.setText(record.getIntensityDisplay());
            if (record.getStartTime() != null) {
                tvTime.setText(timeFormat.format(record.getStartTime()));
            }
            tvDuration.setText(record.getFormattedDuration());
            tvCalories.setText(String.format(Locale.getDefault(), "-%.0f kcal", record.getCaloriesBurned()));
        }
    }
}
