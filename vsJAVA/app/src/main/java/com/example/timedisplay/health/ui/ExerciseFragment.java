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
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.ui.adapter.ExerciseRecordAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

public class ExerciseFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseRecordAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private ExerciseViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        setupViewModel();
        setupListeners();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ExerciseRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ExerciseRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExerciseRecord record) {
                showEditDialog(record);
            }

            @Override
            public void onItemLongClick(ExerciseRecord record) {
                showDeleteConfirm(record);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
            tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        showExerciseDialog(null);
    }

    private void showEditDialog(ExerciseRecord record) {
        showExerciseDialog(record);
    }

    private void showExerciseDialog(ExerciseRecord existingRecord) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_exercise_record, null);
        dialog.setContentView(view);

        Spinner spinnerExerciseType = view.findViewById(R.id.spinnerExerciseType);
        Spinner spinnerIntensity = view.findViewById(R.id.spinnerIntensity);
        Button btnSelectStartTime = view.findViewById(R.id.btnSelectStartTime);
        Button btnSelectEndTime = view.findViewById(R.id.btnSelectEndTime);
        EditText etCalories = view.findViewById(R.id.etCalories);
        EditText etDistance = view.findViewById(R.id.etDistance);
        EditText etNote = view.findViewById(R.id.etNote);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        ArrayAdapter<ExerciseRecord.ExerciseType> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ExerciseRecord.ExerciseType.values());
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(typeAdapter);

        ArrayAdapter<ExerciseRecord.IntensityLevel> intensityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ExerciseRecord.IntensityLevel.values());
        intensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntensity.setAdapter(intensityAdapter);

        if (existingRecord != null) {
            spinnerExerciseType.setSelection(existingRecord.getExerciseType().ordinal());
            spinnerIntensity.setSelection(existingRecord.getIntensity().ordinal());
            etCalories.setText(String.valueOf(existingRecord.getCaloriesBurned()));
            etDistance.setText(String.valueOf(existingRecord.getDistance()));
            etNote.setText(existingRecord.getNote());
            if (existingRecord.getStartTime() != null) {
                startTime.setTime(existingRecord.getStartTime());
            }
            if (existingRecord.getEndTime() != null) {
                endTime.setTime(existingRecord.getEndTime());
            }
        }

        btnSelectStartTime.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        startTime.set(year, month, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                                (view2, hourOfDay, minute) -> {
                                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    startTime.set(Calendar.MINUTE, minute);
                                },
                                startTime.get(Calendar.HOUR_OF_DAY),
                                startTime.get(Calendar.MINUTE),
                                true);
                        timePicker.show();
                    },
                    startTime.get(Calendar.YEAR),
                    startTime.get(Calendar.MONTH),
                    startTime.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnSelectEndTime.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        endTime.set(year, month, dayOfMonth);
                        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                                (view2, hourOfDay, minute) -> {
                                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    endTime.set(Calendar.MINUTE, minute);
                                },
                                endTime.get(Calendar.HOUR_OF_DAY),
                                endTime.get(Calendar.MINUTE),
                                true);
                        timePicker.show();
                    },
                    endTime.get(Calendar.YEAR),
                    endTime.get(Calendar.MONTH),
                    endTime.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnSave.setOnClickListener(v -> {
            String caloriesStr = etCalories.getText().toString().trim();
            String distanceStr = etDistance.getText().toString().trim();

            if (startTime.getTimeInMillis() >= endTime.getTimeInMillis()) {
                Toast.makeText(getContext(), "结束时间必须晚于开始时间", Toast.LENGTH_SHORT).show();
                return;
            }

            ExerciseRecord record = existingRecord != null ? existingRecord : new ExerciseRecord();
            record.setExerciseType((ExerciseRecord.ExerciseType) spinnerExerciseType.getSelectedItem());
            record.setIntensity((ExerciseRecord.IntensityLevel) spinnerIntensity.getSelectedItem());
            record.setStartTime(startTime.getTime());
            record.setEndTime(endTime.getTime());

            if (!caloriesStr.isEmpty()) {
                record.setCaloriesBurned(Double.parseDouble(caloriesStr));
            }
            if (!distanceStr.isEmpty()) {
                record.setDistance(Double.parseDouble(distanceStr));
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

    private void showDeleteConfirm(ExerciseRecord record) {
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
