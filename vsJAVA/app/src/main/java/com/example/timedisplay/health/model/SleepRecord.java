package com.example.timedisplay.health.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timedisplay.health.database.DateConverter;

import java.util.Date;

@Entity(tableName = "sleep_records")
@TypeConverters(DateConverter.class)
public class SleepRecord {
    
    public enum SleepQuality {
        EXCELLENT("优秀", 5),
        GOOD("良好", 4),
        FAIR("一般", 3),
        POOR("较差", 2),
        VERY_POOR("很差", 1);
        
        private final String displayName;
        private final int score;
        
        SleepQuality(String displayName, int score) {
            this.displayName = displayName;
            this.score = score;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getScore() {
            return score;
        }
    }
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private Date sleepTime;
    private Date wakeTime;
    private long durationMinutes;
    private SleepQuality sleepQuality;
    private int wakeUpCount;
    private int sleepLatencyMinutes;
    private boolean hasDream;
    private String dreamDescription;
    private String note;
    private Date recordDate;
    private Date createdAt;
    private Date updatedAt;
    
    public SleepRecord() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.recordDate = new Date();
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Date getSleepTime() {
        return sleepTime;
    }
    
    public void setSleepTime(Date sleepTime) {
        this.sleepTime = sleepTime;
    }
    
    public Date getWakeTime() {
        return wakeTime;
    }
    
    public void setWakeTime(Date wakeTime) {
        this.wakeTime = wakeTime;
        calculateDuration();
    }
    
    public long getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    private void calculateDuration() {
        if (sleepTime != null && wakeTime != null) {
            long diff = wakeTime.getTime() - sleepTime.getTime();
            if (diff < 0) {
                diff += 24 * 60 * 60 * 1000;
            }
            this.durationMinutes = diff / (1000 * 60);
        }
    }
    
    public SleepQuality getSleepQuality() {
        return sleepQuality;
    }
    
    public void setSleepQuality(SleepQuality sleepQuality) {
        this.sleepQuality = sleepQuality;
    }
    
    public int getWakeUpCount() {
        return wakeUpCount;
    }
    
    public void setWakeUpCount(int wakeUpCount) {
        this.wakeUpCount = wakeUpCount;
    }
    
    public int getSleepLatencyMinutes() {
        return sleepLatencyMinutes;
    }
    
    public void setSleepLatencyMinutes(int sleepLatencyMinutes) {
        this.sleepLatencyMinutes = sleepLatencyMinutes;
    }
    
    public boolean isHasDream() {
        return hasDream;
    }
    
    public void setHasDream(boolean hasDream) {
        this.hasDream = hasDream;
    }
    
    public String getDreamDescription() {
        return dreamDescription;
    }
    
    public void setDreamDescription(String dreamDescription) {
        this.dreamDescription = dreamDescription;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public Date getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
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
    
    public String getSleepQualityDisplay() {
        return sleepQuality != null ? sleepQuality.getDisplayName() : "";
    }
    
    public String getFormattedDuration() {
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        }
        return String.format("%d分钟", minutes);
    }
    
    public double getDurationHours() {
        return durationMinutes / 60.0;
    }
}
