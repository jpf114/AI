package com.example.timedisplay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView timeTextView;
    private TextView dateTextView;
    private Handler handler;
    private Runnable timeRunnable;
    private boolean is24HourFormat = true;
    private boolean showDate = true;
    private int fontSize = 72;
    private int themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        handler = new Handler(Looper.getMainLooper());

        startClock();
    }

    private void startClock() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                // 确保时间更新的准确性，使用System.currentTimeMillis()计算下一次执行时间
                long now = System.currentTimeMillis();
                long next = now + 1000 - (now % 1000);
                handler.postDelayed(this, next - now);
            }
        };
        handler.post(timeRunnable);
    }

    private void updateTime() {
        // 使用System.currentTimeMillis()获取当前时间，确保准确性
        long currentTimeMillis = System.currentTimeMillis();
        Date now = new Date(currentTimeMillis);
        String timeFormat = is24HourFormat ? "HH:mm:ss" : "hh:mm:ss a";
        // 设置时区为北京时间（东八区）
        SimpleDateFormat timeSdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        timeSdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
        String time = timeSdf.format(now);
        timeTextView.setText(time);

        if (showDate) {
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
            dateSdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai"));
            String date = dateSdf.format(now);
            dateTextView.setText(date);
        } else {
            dateTextView.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle_format) {
            is24HourFormat = !is24HourFormat;
            updateTime();
            Toast.makeText(this, is24HourFormat ? "已切换到24小时制" : "已切换到12小时制", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_toggle_date) {
            showDate = !showDate;
            updateTime();
            Toast.makeText(this, showDate ? "已显示日期" : "已隐藏日期", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_font_size) {
            cycleFontSize();
            timeTextView.setTextSize(fontSize);
            Toast.makeText(this, "字体大小已调整", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_theme) {
            cycleThemeMode();
            AppCompatDelegate.setDefaultNightMode(themeMode);
            Toast.makeText(this, "主题已切换", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && timeRunnable != null) {
            handler.removeCallbacks(timeRunnable);
        }
    }

    /**
     * 循环切换字体大小：72 → 48 → 96 → 72
     */
    private void cycleFontSize() {
        if (fontSize == 72) {
            fontSize = 48;
        } else if (fontSize == 48) {
            fontSize = 96;
        } else {
            fontSize = 72;
        }
    }

    /**
     * 循环切换主题模式：跟随系统 → 深色模式 → 浅色模式 → 跟随系统
     */
    private void cycleThemeMode() {
        if (themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            themeMode = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}