package com.example.timedisplay.health.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timedisplay.R;
import com.example.timedisplay.health.model.DietRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DietRecordAdapter extends RecyclerView.Adapter<DietRecordAdapter.ViewHolder> {

    private List<DietRecord> records = new ArrayList<>();
    private OnItemClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(DietRecord record);
        void onItemLongClick(DietRecord record);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<DietRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diet_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DietRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFoodName;
        private TextView tvMealType;
        private TextView tvTime;
        private TextView tvCalories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvTime = itemView.findViewById(R.id.tvTime);
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

        void bind(DietRecord record) {
            tvFoodName.setText(record.getFoodName());
            tvMealType.setText(record.getMealTypeDisplay());
            if (record.getIntakeTime() != null) {
                tvTime.setText(timeFormat.format(record.getIntakeTime()));
            }
            tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", record.getCalories()));
        }
    }
}
