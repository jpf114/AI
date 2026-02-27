package com.example.timedisplay.health.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timedisplay.R;
import com.example.timedisplay.health.database.EncryptionUtil;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;
import com.example.timedisplay.health.pdf.PdfReportGenerator;
import com.example.timedisplay.health.stats.HealthStatistics;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private TextView tvDietStats;
    private TextView tvExerciseStats;
    private TextView tvSleepStats;
    private TextView tvSuggestions;
    private Button btnSelectStartDate;
    private Button btnSelectEndDate;
    private Button btnGeneratePdf;
    private RadioGroup rgPeriodType;
    private EditText etPassword;

    private StatisticsViewModel viewModel;
    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        tvDietStats = view.findViewById(R.id.tvDietStats);
        tvExerciseStats = view.findViewById(R.id.tvExerciseStats);
        tvSleepStats = view.findViewById(R.id.tvSleepStats);
        tvSuggestions = view.findViewById(R.id.tvSuggestions);
        btnSelectStartDate = view.findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = view.findViewById(R.id.btnSelectEndDate);
        btnGeneratePdf = view.findViewById(R.id.btnGeneratePdf);
        rgPeriodType = view.findViewById(R.id.rgPeriodType);
        etPassword = view.findViewById(R.id.etPassword);

        setupViewModel();
        setupListeners();
        setDefaultDateRange();

        return view;
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        loadStatistics();
    }

    private void setupListeners() {
        btnSelectStartDate.setOnClickListener(v -> showDatePicker(startDate, true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(endDate, false));

        rgPeriodType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbWeek) {
                setWeekRange();
            } else if (checkedId == R.id.rbMonth) {
                setMonthRange();
            }
            loadStatistics();
        });

        btnGeneratePdf.setOnClickListener(v -> generatePdf());
    }

    private void setDefaultDateRange() {
        setWeekRange();
    }

    private void setWeekRange() {
        endDate.setTime(new Date());
        startDate.setTime(new Date());
        startDate.add(Calendar.DAY_OF_YEAR, -7);
        updateDateButtons();
    }

    private void setMonthRange() {
        endDate.setTime(new Date());
        startDate.setTime(new Date());
        startDate.add(Calendar.MONTH, -1);
        updateDateButtons();
    }

    private void updateDateButtons() {
        btnSelectStartDate.setText(dateFormat.format(startDate.getTime()));
        btnSelectEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void showDatePicker(Calendar calendar, boolean isStartDate) {
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    updateDateButtons();
                    loadStatistics();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void loadStatistics() {
        Date start = startDate.getTime();
        Date end = endDate.getTime();

        viewModel.getDietRecords(start, end).observe(getViewLifecycleOwner(), dietRecords -> {
            viewModel.getExerciseRecords(start, end).observe(getViewLifecycleOwner(), exerciseRecords -> {
                viewModel.getSleepRecords(start, end).observe(getViewLifecycleOwner(), sleepRecords -> {
                    updateStatistics(dietRecords, exerciseRecords, sleepRecords);
                });
            });
        });
    }

    private void updateStatistics(List<DietRecord> dietRecords, List<ExerciseRecord> exerciseRecords, List<SleepRecord> sleepRecords) {
        HealthStatistics.ComprehensiveStats stats = HealthStatistics.generateComprehensiveStats(
                dietRecords, exerciseRecords, sleepRecords, startDate.getTime(), endDate.getTime(), getPeriodType());

        String dietText = String.format("饮食统计:\n" +
                        "  记录数: %d\n" +
                        "  总热量: %.0f kcal\n" +
                        "  日均热量: %.0f kcal\n" +
                        "  蛋白质: %.1fg  碳水: %.1fg  脂肪: %.1fg",
                stats.dietStats.recordCount,
                stats.dietStats.totalCalories,
                stats.dietStats.avgCaloriesPerDay,
                stats.dietStats.totalProtein,
                stats.dietStats.totalCarbs,
                stats.dietStats.totalFat);
        tvDietStats.setText(dietText);

        String exerciseText = String.format("运动统计:\n" +
                        "  记录数: %d\n" +
                        "  总消耗: %.0f kcal\n" +
                        "  日均消耗: %.0f kcal\n" +
                        "  总时长: %d分钟\n" +
                        "  日均时长: %d分钟",
                stats.exerciseStats.recordCount,
                stats.exerciseStats.totalCaloriesBurned,
                stats.exerciseStats.avgCaloriesPerDay,
                stats.exerciseStats.totalDurationMinutes,
                stats.exerciseStats.avgDurationPerDay);
        tvExerciseStats.setText(exerciseText);

        String sleepText = String.format("睡眠统计:\n" +
                        "  记录数: %d\n" +
                        "  平均时长: %.1f小时\n" +
                        "  平均质量: %s (%.1f分)\n" +
                        "  总夜醒次数: %d",
                stats.sleepStats.recordCount,
                stats.sleepStats.avgDurationHours,
                HealthStatistics.getQualityDescription(stats.sleepStats.avgQualityScore),
                stats.sleepStats.avgQualityScore,
                stats.sleepStats.totalWakeUpCount);
        tvSleepStats.setText(sleepText);

        StringBuilder suggestions = new StringBuilder("健康建议:\n");
        suggestions.append("• ").append(HealthStatistics.getDietSuggestion(stats.dietStats.avgCaloriesPerDay)).append("\n");
        suggestions.append("• ").append(HealthStatistics.getExerciseSuggestion(stats.exerciseStats.avgCaloriesPerDay, stats.exerciseStats.avgDurationPerDay)).append("\n");
        suggestions.append("• ").append(HealthStatistics.getSleepSuggestion(stats.sleepStats.avgDurationHours));
        tvSuggestions.setText(suggestions.toString());
    }

    private String getPeriodType() {
        int checkedId = rgPeriodType.getCheckedRadioButtonId();
        return checkedId == R.id.rbWeek ? "周" : "月";
    }

    private void generatePdf() {
        String password = etPassword.getText().toString().trim();
        if (!password.isEmpty()) {
            EncryptionUtil.savePassword(password);
        }

        Date start = startDate.getTime();
        Date end = endDate.getTime();

        viewModel.getDietRecords(start, end).observe(getViewLifecycleOwner(), dietRecords -> {
            viewModel.getExerciseRecords(start, end).observe(getViewLifecycleOwner(), exerciseRecords -> {
                viewModel.getSleepRecords(start, end).observe(getViewLifecycleOwner(), sleepRecords -> {
                    PdfReportGenerator.generateHealthReport(
                            requireContext(),
                            dietRecords,
                            exerciseRecords,
                            sleepRecords,
                            start,
                            end,
                            getPeriodType(),
                            password.isEmpty() ? null : password,
                            new PdfReportGenerator.PdfGenerationCallback() {
                                @Override
                                public void onSuccess(String filePath) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "PDF已生成: " + filePath, Toast.LENGTH_LONG).show();
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(getContext(), "生成失败: " + error, Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                    );
                });
            });
        });
    }
}
