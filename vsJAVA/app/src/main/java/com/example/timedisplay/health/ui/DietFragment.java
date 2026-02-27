package com.example.timedisplay.health.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.FoodItem;
import com.example.timedisplay.health.ui.adapter.DietRecordAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DietFragment extends Fragment {

    private RecyclerView recyclerView;
    private DietRecordAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvEmpty;
    private DietViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        fabAdd = view.findViewById(R.id.fabAdd);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        setupViewModel();
        setupListeners();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new DietRecordAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new DietRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DietRecord record) {
                showEditDialog(record);
            }

            @Override
            public void onItemLongClick(DietRecord record) {
                showDeleteConfirm(record);
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DietViewModel.class);
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
            tvEmpty.setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        showDietDialog(null);
    }

    private void showEditDialog(DietRecord record) {
        showDietDialog(record);
    }

    private void showDietDialog(DietRecord existingRecord) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_diet_record, null);
        dialog.setContentView(view);

        AutoCompleteTextView etFoodName = view.findViewById(R.id.etFoodName);
        Spinner spinnerMealType = view.findViewById(R.id.spinnerMealType);
        Button btnSelectTime = view.findViewById(R.id.btnSelectTime);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etCalories = view.findViewById(R.id.etCalories);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar selectedTime = Calendar.getInstance();

        ArrayAdapter<DietRecord.MealType> mealAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, DietRecord.MealType.values());
        mealAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(mealAdapter);

        viewModel.getAllFoodItems().observe(getViewLifecycleOwner(), foodItems -> {
            List<String> foodNames = new ArrayList<>();
            for (FoodItem item : foodItems) {
                foodNames.add(item.getName());
            }
            ArrayAdapter<String> foodAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, foodNames);
            etFoodName.setAdapter(foodAdapter);
        });

        if (existingRecord != null) {
            etFoodName.setText(existingRecord.getFoodName());
            spinnerMealType.setSelection(existingRecord.getMealType().ordinal());
            etAmount.setText(String.valueOf(existingRecord.getAmount()));
            etCalories.setText(String.valueOf(existingRecord.getCalories()));
            if (existingRecord.getIntakeTime() != null) {
                selectedTime.setTime(existingRecord.getIntakeTime());
            }
        }

        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                    (view1, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    true);
            timePicker.show();
        });

        btnSave.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();

            if (foodName.isEmpty()) {
                etFoodName.setError("请输入食物名称");
                return;
            }

            DietRecord record = existingRecord != null ? existingRecord : new DietRecord();
            record.setFoodName(foodName);
            record.setMealType((DietRecord.MealType) spinnerMealType.getSelectedItem());
            record.setIntakeTime(selectedTime.getTime());

            if (!amountStr.isEmpty()) {
                record.setAmount(Double.parseDouble(amountStr));
            }
            if (!caloriesStr.isEmpty()) {
                record.setCalories(Double.parseDouble(caloriesStr));
            }

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

    private void showDeleteConfirm(DietRecord record) {
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
