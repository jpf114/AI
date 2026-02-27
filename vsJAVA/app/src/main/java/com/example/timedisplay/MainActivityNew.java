package com.example.timedisplay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;
import com.example.timedisplay.health.pdf.PdfReportGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivityNew extends AppCompatActivity {

    private ClockView clockView;
    private FrameLayout clockContainer;
    private TextView tvCurrentCategory;
    private TextView tvLastRecord;
    private BottomNavigationView bottomNavigation;
    private View coordinatorLayout;
    private ImageButton btnTheme;
    private ImageButton btnStatistics;
    private ImageButton btnExport;

    private int currentCategory = 0; // 0: 饮食, 1: 运动, 2: 作息
    private int[] themeColors = {
            Color.parseColor("#FF8C42"), // 饮食 - 橙色
            Color.parseColor("#4CAF50"), // 运动 - 绿色
            Color.parseColor("#5C6BC0")  // 作息 - 蓝色
    };
    private int[] backgroundColors = {
            Color.parseColor("#FFF3E0"), // 饮食背景
            Color.parseColor("#E8F5E9"), // 运动背景
            Color.parseColor("#E8EAF6")  // 作息背景
    };
    private String[] categoryNames = {"饮食记录", "运动记录", "作息记录"};
    private String[] categoryHints = {"点击记录饮食", "点击记录运动", "点击记录作息"};

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Date lastRecordTime;
    private ThemeManager themeManager;
    private StatisticsChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 应用保存的主题
        themeManager = new ThemeManager(this);
        themeManager.applySavedTheme();
        
        setContentView(R.layout.activity_main_new);

        initViews();
        setupBottomNavigation();
        setupClockClickListener();
        setupTopRightButtons();
        updateTheme(0);
    }

    private void initViews() {
        clockView = findViewById(R.id.clockView);
        clockContainer = findViewById(R.id.clockContainer);
        tvCurrentCategory = findViewById(R.id.tvCurrentCategory);
        tvLastRecord = findViewById(R.id.tvLastRecord);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        btnTheme = findViewById(R.id.btnTheme);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnExport = findViewById(R.id.btnExport);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_diet) {
                updateTheme(0);
                return true;
            } else if (itemId == R.id.nav_exercise) {
                updateTheme(1);
                return true;
            } else if (itemId == R.id.nav_sleep) {
                updateTheme(2);
                return true;
            }
            return false;
        });
    }

    private void setupTopRightButtons() {
        // 主题切换按钮
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> showThemeSelector());
        }

        // 统计分析按钮
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> showStatisticsDialog());
        }

        // 导出按钮
        if (btnExport != null) {
            btnExport.setOnClickListener(v -> showExportMenu(v));
        }
    }

    private void showThemeSelector() {
        String[] themes = {"浅色主题", "深色主题", "跟随系统"};
        int currentMode = themeManager.getThemeMode();
        
        new AlertDialog.Builder(this)
                .setTitle("选择主题")
                .setSingleChoiceItems(themes, currentMode, (dialog, which) -> {
                    themeManager.setThemeMode(which);
                    Toast.makeText(this, "已切换到" + themes[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showStatisticsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_statistics, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // 初始化视图
        TabLayout tabCategory = view.findViewById(R.id.tabCategory);
        Button btnDay = view.findViewById(R.id.btnDay);
        Button btnWeek = view.findViewById(R.id.btnWeek);
        Button btnMonth = view.findViewById(R.id.btnMonth);
        Button btnCustom = view.findViewById(R.id.btnCustom);
        Button btnClose = view.findViewById(R.id.btnClose);
        StatisticsChartView chartView = view.findViewById(R.id.chartView);
        TextView tvNoData = view.findViewById(R.id.tvNoData);
        TextView tvMetric1Value = view.findViewById(R.id.tvMetric1Value);
        TextView tvMetric2Value = view.findViewById(R.id.tvMetric2Value);
        TextView tvMetric3Value = view.findViewById(R.id.tvMetric3Value);

        // 检查视图是否初始化成功
        if (tabCategory == null || chartView == null) {
            Toast.makeText(this, "界面初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置图表主题色
        chartView.setThemeColor(themeColors[currentCategory]);

        // 类别切换
        tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                chartView.setThemeColor(themeColors[position]);
                loadStatisticsData(position, "week", tvMetric1Value, tvMetric2Value, tvMetric3Value);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 时间范围选择
        View.OnClickListener timeRangeListener = v -> {
            String range = "week";
            if (v == btnDay) range = "day";
            else if (v == btnWeek) range = "week";
            else if (v == btnMonth) range = "month";
            else if (v == btnCustom) range = "custom";
            
            loadStatisticsData(tabCategory.getSelectedTabPosition(), range, 
                    tvMetric1Value, tvMetric2Value, tvMetric3Value);
        };
        
        if (btnDay != null) btnDay.setOnClickListener(timeRangeListener);
        if (btnWeek != null) btnWeek.setOnClickListener(timeRangeListener);
        if (btnMonth != null) btnMonth.setOnClickListener(timeRangeListener);
        if (btnCustom != null) btnCustom.setOnClickListener(timeRangeListener);

        // 关闭按钮
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        // 加载初始数据
        loadStatisticsData(0, "week", tvMetric1Value, tvMetric2Value, tvMetric3Value);

        dialog.show();
    }

    private void loadStatisticsData(int category, String timeRange, 
                                    TextView tvMetric1, TextView tvMetric2, TextView tvMetric3) {
        // 检查TextView是否为null
        if (tvMetric1 == null || tvMetric2 == null || tvMetric3 == null) {
            return;
        }
        
        // 在UI线程显示加载中
        runOnUiThread(() -> {
            tvMetric1.setText("-");
            tvMetric2.setText("-");
            tvMetric3.setText("加载中...");
        });
        
        // 在后台线程加载数据
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Date endDate = new Date();
                Date startDate = getStartDateForRange(timeRange);
                
                int count = 0;
                double avg = 0;
                String trend = "→";
                
                if (category == 0) {
                    // 饮食统计
                    List<DietRecord> records = HealthDatabase.getDatabase(this)
                            .healthDao().getDietRecordsBetweenSync(startDate, endDate);
                    count = records.size();
                    double totalCalories = 0;
                    for (DietRecord r : records) totalCalories += r.getCalories();
                    avg = count > 0 ? totalCalories / count : 0;
                } else if (category == 1) {
                    // 运动统计
                    List<ExerciseRecord> records = HealthDatabase.getDatabase(this)
                            .healthDao().getExerciseRecordsBetweenSync(startDate, endDate);
                    count = records.size();
                    long totalDuration = 0;
                    for (ExerciseRecord r : records) totalDuration += r.getDurationMinutes();
                    avg = count > 0 ? totalDuration / count : 0;
                } else if (category == 2) {
                    // 作息统计
                    List<SleepRecord> records = HealthDatabase.getDatabase(this)
                            .healthDao().getSleepRecordsBetweenSync(startDate, endDate);
                    count = records.size();
                    double totalHours = 0;
                    for (SleepRecord r : records) totalHours += r.getDurationHours();
                    avg = count > 0 ? totalHours / count : 0;
                }
                
                final int finalCount = count;
                final double finalAvg = avg;
                final String finalTrend = trend;
                
                runOnUiThread(() -> {
                    try {
                        tvMetric1.setText(String.valueOf(finalCount));
                        tvMetric2.setText(String.format(Locale.getDefault(), "%.1f", finalAvg));
                        tvMetric3.setText(finalTrend);
                    } catch (Exception e) {
                        // 忽略UI更新错误
                    }
                });
            } catch (Exception e) {
                // 数据库查询出错
                runOnUiThread(() -> {
                    try {
                        tvMetric1.setText("0");
                        tvMetric2.setText("0");
                        tvMetric3.setText("错误");
                    } catch (Exception ex) {
                        // 忽略UI更新错误
                    }
                });
            }
        });
    }

    private Date getStartDateForRange(String range) {
        Calendar cal = Calendar.getInstance();
        switch (range) {
            case "day":
                cal.add(Calendar.DAY_OF_YEAR, -1);
                break;
            case "week":
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "month":
                cal.add(Calendar.MONTH, -1);
                break;
            case "custom":
                cal.add(Calendar.MONTH, -3);
                break;
        }
        return cal.getTime();
    }

    private void showExportMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.export_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.export_pdf) {
                exportAsPdf();
                return true;
            } else if (itemId == R.id.export_image) {
                exportAsImage();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void exportAsPdf() {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setMessage("正在生成PDF...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        Date endDate = new Date();
        Date startDate = new Date();
        startDate.setTime(endDate.getTime() - 7 * 24 * 60 * 60 * 1000);

        HealthDatabase.databaseWriteExecutor.execute(() -> {
            List<DietRecord> dietRecords = HealthDatabase.getDatabase(this)
                    .healthDao().getDietRecordsBetweenSync(startDate, endDate);
            List<ExerciseRecord> exerciseRecords = HealthDatabase.getDatabase(this)
                    .healthDao().getExerciseRecordsBetweenSync(startDate, endDate);

            PdfReportGenerator.generateHealthReport(
                    this,
                    dietRecords,
                    exerciseRecords,
                    new ArrayList<>(),
                    startDate,
                    endDate,
                    "周",
                    null,
                    new PdfReportGenerator.PdfGenerationCallback() {
                        @Override
                        public void onSuccess(String filePath) {
                            progressDialog.dismiss();
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivityNew.this,
                                        "PDF已保存: " + filePath, Toast.LENGTH_LONG).show();
                                shareFile(new File(filePath), "application/pdf");
                            });
                        }

                        @Override
                        public void onError(String error) {
                            progressDialog.dismiss();
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivityNew.this,
                                        "导出失败: " + error, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
            );
        });
    }

    private void exportAsImage() {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setMessage("正在生成图片...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // 截取时钟区域
                Bitmap bitmap = Bitmap.createBitmap(
                        clockContainer.getWidth(),
                        clockContainer.getHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                clockContainer.draw(canvas);

                // 保存图片
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "HealthClock");
                if (!appDir.exists()) appDir.mkdirs();

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File imageFile = new File(appDir, "health_record_" + timeStamp + ".png");

                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                progressDialog.dismiss();
                Toast.makeText(this, "图片已保存: " + imageFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                shareFile(imageFile, "image/png");

            } catch (IOException e) {
                progressDialog.dismiss();
                Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, 500);
    }

    private void shareFile(File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".provider", file);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(shareIntent, "分享文件"));
    }

    private void updateTheme(int category) {
        currentCategory = category;
        int themeColor = themeColors[category];
        int bgColor = backgroundColors[category];

        // 更新时钟主题色
        if (clockView != null) {
            clockView.setThemeColor(themeColor);
        }

        // 更新时钟容器背景
        if (clockContainer != null) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(bgColor);
            drawable.setStroke(8, themeColor);
            clockContainer.setBackground(drawable);
        }

        // 更新文字颜色
        if (tvCurrentCategory != null) {
            tvCurrentCategory.setTextColor(themeColor);
            tvCurrentCategory.setText("当前: " + categoryNames[category]);
        }

        // 更新底部导航选中颜色
        if (bottomNavigation != null) {
            bottomNavigation.setItemIconTintList(createColorStateList(themeColor));
            bottomNavigation.setItemTextColor(createColorStateList(themeColor));
        }

        // 更新提示文字
        if (tvLastRecord != null) {
            tvLastRecord.setText(categoryHints[category]);
        }
    }

    private android.content.res.ColorStateList createColorStateList(int color) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] colors = new int[]{color, android.graphics.Color.GRAY};
        return new android.content.res.ColorStateList(states, colors);
    }

    private void setupClockClickListener() {
        if (clockContainer == null) return;
        
        clockContainer.setOnClickListener(v -> {
            lastRecordTime = new Date();
            showRecordDialog();
        });
    }

    private void showRecordDialog() {
        if (lastRecordTime == null) {
            lastRecordTime = new Date();
        }
        
        switch (currentCategory) {
            case 0:
                showDietDialog();
                break;
            case 1:
                showExerciseDialog();
                break;
            case 2:
                showSleepDialog();
                break;
        }
    }

    private void showDietDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_diet_quick, null);
        builder.setView(view);

        EditText etFoodName = view.findViewById(R.id.etFoodName);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etCalories = view.findViewById(R.id.etCalories);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        tvTime.setText("记录时间: " + timeFormat.format(lastRecordTime));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();

            if (foodName.isEmpty()) {
                etFoodName.setError("请输入食物名称");
                return;
            }

            DietRecord record = new DietRecord();
            record.setFoodName(foodName);
            record.setMealType(DietRecord.MealType.SNACK);
            record.setIntakeTime(lastRecordTime);
            if (!amountStr.isEmpty()) {
                record.setAmount(Double.parseDouble(amountStr));
            }
            if (!caloriesStr.isEmpty()) {
                record.setCalories(Double.parseDouble(caloriesStr));
            }

            saveDietRecord(record);
            updateLastRecord("饮食: " + foodName + " " + timeFormat.format(lastRecordTime));
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showExerciseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_exercise_quick, null);
        builder.setView(view);

        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etCalories = view.findViewById(R.id.etCalories);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // 设置运动类型适配器
        ArrayAdapter<ExerciseRecord.ExerciseType> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ExerciseRecord.ExerciseType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        tvTime.setText("记录时间: " + timeFormat.format(lastRecordTime));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String durationStr = etDuration.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();

            ExerciseRecord record = new ExerciseRecord();
            record.setExerciseType((ExerciseRecord.ExerciseType) spinnerType.getSelectedItem());
            record.setStartTime(lastRecordTime);
            if (!durationStr.isEmpty()) {
                record.setDurationMinutes(Long.parseLong(durationStr));
            }
            if (!caloriesStr.isEmpty()) {
                record.setCaloriesBurned(Double.parseDouble(caloriesStr));
            }

            saveExerciseRecord(record);
            updateLastRecord("运动: " + record.getExerciseTypeDisplay() + " " + timeFormat.format(lastRecordTime));
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSleepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_sleep_quick, null);
        builder.setView(view);

        Spinner spinnerActivity = view.findViewById(R.id.spinnerActivity);
        EditText etNote = view.findViewById(R.id.etNote);
        TextView tvTime = view.findViewById(R.id.tvTime);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // 设置活动类型适配器
        String[] activities = {"入睡", "起床", "午休", "小憩", "其他"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, activities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(adapter);

        tvTime.setText("记录时间: " + timeFormat.format(lastRecordTime));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String activity = spinnerActivity.getSelectedItem().toString();
            String note = etNote.getText().toString().trim();

            updateLastRecord("作息: " + activity + " " + timeFormat.format(lastRecordTime));
            Toast.makeText(this, "记录已保存", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveDietRecord(DietRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            HealthDatabase.getDatabase(this).healthDao().insertDietRecord(record);
            runOnUiThread(() -> Toast.makeText(this, "饮食记录已保存", Toast.LENGTH_SHORT).show());
        });
    }

    private void saveExerciseRecord(ExerciseRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            HealthDatabase.getDatabase(this).healthDao().insertExerciseRecord(record);
            runOnUiThread(() -> Toast.makeText(this, "运动记录已保存", Toast.LENGTH_SHORT).show());
        });
    }

    private void updateLastRecord(String text) {
        tvLastRecord.setText("最近记录: " + text);
    }
}
