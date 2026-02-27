package com.example.timedisplay;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {
    private static final String PREFS_NAME = "health_data_prefs";
    private static final String KEY_LAST_BACKUP = "last_backup_time";
    
    private Context context;
    private SharedPreferences prefs;
    private HealthDatabase database;
    
    public DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.database = HealthDatabase.getDatabase(context);
    }
    
    // 导出数据为JSON格式
    public String exportToJson(Date startDate, Date endDate) {
        try {
            JSONObject root = new JSONObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
            // 导出饮食记录
            List<DietRecord> dietRecords = database.healthDao()
                    .getDietRecordsBetweenSync(startDate, endDate);
            JSONArray dietArray = new JSONArray();
            for (DietRecord record : dietRecords) {
                JSONObject obj = new JSONObject();
                obj.put("id", record.getId());
                obj.put("foodName", record.getFoodName());
                obj.put("mealType", record.getMealType() != null ? record.getMealType().name() : "");
                obj.put("intakeTime", record.getIntakeTime() != null ? sdf.format(record.getIntakeTime()) : "");
                obj.put("calories", record.getCalories());
                obj.put("amount", record.getAmount());
                dietArray.put(obj);
            }
            root.put("dietRecords", dietArray);
            
            // 导出运动记录
            List<ExerciseRecord> exerciseRecords = database.healthDao()
                    .getExerciseRecordsBetweenSync(startDate, endDate);
            JSONArray exerciseArray = new JSONArray();
            for (ExerciseRecord record : exerciseRecords) {
                JSONObject obj = new JSONObject();
                obj.put("id", record.getId());
                obj.put("exerciseType", record.getExerciseType() != null ? record.getExerciseType().name() : "");
                obj.put("startTime", record.getStartTime() != null ? sdf.format(record.getStartTime()) : "");
                obj.put("durationMinutes", record.getDurationMinutes());
                obj.put("caloriesBurned", record.getCaloriesBurned());
                exerciseArray.put(obj);
            }
            root.put("exerciseRecords", exerciseArray);
            
            // 导出睡眠记录
            List<SleepRecord> sleepRecords = database.healthDao()
                    .getSleepRecordsBetweenSync(startDate, endDate);
            JSONArray sleepArray = new JSONArray();
            for (SleepRecord record : sleepRecords) {
                JSONObject obj = new JSONObject();
                obj.put("id", record.getId());
                obj.put("sleepTime", record.getSleepTime() != null ? sdf.format(record.getSleepTime()) : "");
                obj.put("wakeTime", record.getWakeTime() != null ? sdf.format(record.getWakeTime()) : "");
                obj.put("durationHours", record.getDurationHours());
                obj.put("sleepQuality", record.getSleepQuality() != null ? record.getSleepQuality().name() : "");
                sleepArray.put(obj);
            }
            root.put("sleepRecords", sleepArray);
            
            // 元数据
            JSONObject metadata = new JSONObject();
            metadata.put("exportTime", sdf.format(new Date()));
            metadata.put("startDate", sdf.format(startDate));
            metadata.put("endDate", sdf.format(endDate));
            metadata.put("version", "1.0");
            root.put("metadata", metadata);
            
            return root.toString(2);
            
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 保存JSON文件到本地
    public File saveJsonToFile(String jsonContent, String fileName) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), "exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        File file = new File(dir, fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(jsonContent);
        writer.close();
        
        // 更新最后备份时间
        prefs.edit().putLong(KEY_LAST_BACKUP, System.currentTimeMillis()).apply();
        
        return file;
    }
    
    // 获取统计数据摘要
    public StatisticsSummary getStatisticsSummary(Date startDate, Date endDate) {
        StatisticsSummary summary = new StatisticsSummary();
        
        List<DietRecord> dietRecords = database.healthDao()
                .getDietRecordsBetweenSync(startDate, endDate);
        summary.dietCount = dietRecords.size();
        summary.totalCalories = 0;
        for (DietRecord r : dietRecords) {
            summary.totalCalories += r.getCalories();
        }
        
        List<ExerciseRecord> exerciseRecords = database.healthDao()
                .getExerciseRecordsBetweenSync(startDate, endDate);
        summary.exerciseCount = exerciseRecords.size();
        summary.totalExerciseMinutes = 0;
        for (ExerciseRecord r : exerciseRecords) {
            summary.totalExerciseMinutes += r.getDurationMinutes();
        }
        
        List<SleepRecord> sleepRecords = database.healthDao()
                .getSleepRecordsBetweenSync(startDate, endDate);
        summary.sleepCount = sleepRecords.size();
        summary.totalSleepHours = 0;
        for (SleepRecord r : sleepRecords) {
            summary.totalSleepHours += r.getDurationHours();
        }
        
        return summary;
    }
    
    // 获取最后备份时间
    public long getLastBackupTime() {
        return prefs.getLong(KEY_LAST_BACKUP, 0);
    }
    
    // 统计数据摘要类
    public static class StatisticsSummary {
        public int dietCount;
        public double totalCalories;
        public int exerciseCount;
        public long totalExerciseMinutes;
        public int sleepCount;
        public double totalSleepHours;
    }
}
