package com.example.timedisplay.health.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timedisplay.health.database.DateConverter;

import java.util.Date;

@Entity(tableName = "exercise_records")
@TypeConverters(DateConverter.class)
public class ExerciseRecord {
    
    public enum ExerciseType {
        RUNNING("跑步"),
        WALKING("步行"),
        CYCLING("骑行"),
        SWIMMING("游泳"),
        YOGA("瑜伽"),
        GYM("健身"),
        BASKETBALL("篮球"),
        FOOTBALL("足球"),
        BADMINTON("羽毛球"),
        TENNIS("网球"),
        HIKING("徒步"),
        DANCING("舞蹈"),
        OTHER("其他");
        
        private final String displayName;
        
        ExerciseType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum IntensityLevel {
        LOW("低强度", 1),
        MEDIUM("中等强度", 2),
        HIGH("高强度", 3),
        VERY_HIGH("极高强度", 4);
        
        private final String displayName;
        private final int level;
        
        IntensityLevel(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private ExerciseType exerciseType;
    private String customTypeName;
    private Date startTime;
    private Date endTime;
    private long durationMinutes;
    private IntensityLevel intensity;
    private double caloriesBurned;
    private double distance;
    private int steps;
    private String note;
    private Date createdAt;
    private Date updatedAt;
    
    public ExerciseRecord() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public ExerciseType getExerciseType() {
        return exerciseType;
    }
    
    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }
    
    public String getCustomTypeName() {
        return customTypeName;
    }
    
    public void setCustomTypeName(String customTypeName) {
        this.customTypeName = customTypeName;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        calculateDuration();
    }
    
    public long getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMinutes = (endTime.getTime() - startTime.getTime()) / (1000 * 60);
        }
    }
    
    public IntensityLevel getIntensity() {
        return intensity;
    }
    
    public void setIntensity(IntensityLevel intensity) {
        this.intensity = intensity;
    }
    
    public double getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    public int getSteps() {
        return steps;
    }
    
    public void setSteps(int steps) {
        this.steps = steps;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getExerciseTypeDisplay() {
        if (exerciseType == ExerciseType.OTHER && customTypeName != null && !customTypeName.isEmpty()) {
            return customTypeName;
        }
        return exerciseType != null ? exerciseType.getDisplayName() : "";
    }
    
    public String getIntensityDisplay() {
        return intensity != null ? intensity.getDisplayName() : "";
    }
    
    public String getFormattedDuration() {
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        }
        return String.format("%d分钟", minutes);
    }
}
