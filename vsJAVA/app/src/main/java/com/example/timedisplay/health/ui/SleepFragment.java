package com.example.timedisplay.health.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timedisplay.R;
import com.example.timedisplay.health.model.SleepRecord;
import com.example.timedisplay.health.ui.adapter.SleepRecordAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

public class SleepFragment extends Fragment {

    private RecyclerView recyclerView;
    private SleepRecordAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private SleepViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        setupViewModel();
        setupListeners();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new SleepRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SleepRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SleepRecord record) {
                showEditDialog(record);
            }

            @Override
            public void onItemLongClick(SleepRecord record) {
                showDeleteConfirm(record);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SleepViewModel.class);
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
            tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        showSleepDialog(null);
    }

    private void showEditDialog(SleepRecord record) {
        showSleepDialog(record);
    }

    private void showSleepDialog(SleepRecord existingRecord) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_sleep_record, null);
        dialog.setContentView(view);

        Button btnSelectSleepTime = view.findViewById(R.id.btnSelectSleepTime);
        Button btnSelectWakeTime = view.findViewById(R.id.btnSelectWakeTime);
        Spinner spinnerQuality = view.findViewById(R.id.spinnerQuality);
        EditText etWakeUpCount = view.findViewById(R.id.etWakeUpCount);
        EditText etNote = view.findViewById(R.id.etNote);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar sleepTime = Calendar.getInstance();
        Calendar wakeTime = Calendar.getInstance();
        wakeTime.add(Calendar.HOUR, 8);

        ArrayAdapter<SleepRecord.SleepQuality> qualityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, SleepRecord.SleepQuality.values());
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuality.setAdapter(qualityAdapter);

        if (existingRecord != null) {
            spinnerQuality.setSelection(existingRecord.getSleepQuality().ordinal());
            etWakeUpCount.setText(String.valueOf(existingRecord.getWakeUpCount()));
            etNote.setText(existingRecord.getNote());
            if (existingRecord.getSleepTime() != null) {
                sleepTime.setTime(existingRecord.getSleepTime());
            }
            if (existingRecord.getWakeTime() != null) {
                wakeTime.setTime(existingRecord.getWakeTime());
            }
        }

        btnSelectSleepTime.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        sleepTime.set(year, month, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                                (view2, hourOfDay, minute) -> {
                                    sleepTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    sleepTime.set(Calendar.MINUTE, minute);
                                },
                                sleepTime.get(Calendar.HOUR_OF_DAY),
                                sleepTime.get(Calendar.MINUTE),
                                true);
                        timePicker.show();
                    },
                    sleepTime.get(Calendar.YEAR),
                    sleepTime.get(Calendar.MONTH),
                    sleepTime.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnSelectWakeTime.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        wakeTime.set(year, month, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                                (view2, hourOfDay, minute) -> {
                                    wakeTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    wakeTime.set(Calendar.MINUTE, minute);
                                },
                                wakeTime.get(Calendar.HOUR_OF_DAY),
                                wakeTime.get(Calendar.MINUTE),
                                true);
                        timePicker.show();
                    },
                    wakeTime.get(Calendar.YEAR),
                    wakeTime.get(Calendar.MONTH),
                    wakeTime.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnSave.setOnClickListener(v -> {
            String wakeUpCountStr = etWakeUpCount.getText().toString().trim();

            SleepRecord record = existingRecord != null ? existingRecord : new SleepRecord();
            record.setSleepTime(sleepTime.getTime());
            record.setWakeTime(wakeTime.getTime());
            record.setSleepQuality((SleepRecord.SleepQuality) spinnerQuality.getSelectedItem());

            if (!wakeUpCountStr.isEmpty()) {
                record.setWakeUpCount(Integer.parseInt(wakeUpCountStr));
            }
            record.setNote(etNote.getText().toString().trim());
            record.setUpdatedAt(new Date());

            if (existingRecord != null) {
                viewModel.update(record);
            } else {
                viewModel.insert(record);
            }

            dialog.dismiss();
            Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showDeleteConfirm(SleepRecord record) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除这条记录吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.delete(record);
                    Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
