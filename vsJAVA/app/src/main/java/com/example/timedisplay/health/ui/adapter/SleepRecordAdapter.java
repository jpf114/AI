package com.example.timedisplay.health.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timedisplay.R;
import com.example.timedisplay.health.model.SleepRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SleepRecordAdapter extends RecyclerView.Adapter<SleepRecordAdapter.ViewHolder> {

    private List<SleepRecord> records = new ArrayList<>();
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(SleepRecord record);
        void onItemLongClick(SleepRecord record);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<SleepRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sleep_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SleepRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvDuration;
        private TextView tvQuality;
        private TextView tvWakeUpCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvQuality = itemView.findViewById(R.id.tvQuality);
            tvWakeUpCount = itemView.findViewById(R.id.tvWakeUpCount);

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

        void bind(SleepRecord record) {
            if (record.getRecordDate() != null) {
                tvDate.setText(dateFormat.format(record.getRecordDate()));
            }
            tvDuration.setText(record.getFormattedDuration());
            tvQuality.setText(record.getSleepQualityDisplay());
            tvWakeUpCount.setText(String.format(Locale.getDefault(), "夜醒%d次", record.getWakeUpCount()));
        }
    }
}
