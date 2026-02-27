package com.example.timedisplay.health.stats;

import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;

import java.util.Date;
import java.util.List;

public class HealthStatistics {
    
    public static class DietStats {
        public double totalCalories;
        public double avgCaloriesPerDay;
        public double totalProtein;
        public double totalCarbs;
        public double totalFat;
        public int recordCount;
        public double[] dailyCalories;
        public String[] dates;
        
        public DietStats(int days) {
            this.dailyCalories = new double[days];
            this.dates = new String[days];
        }
    }
    
    public static class ExerciseStats {
        public double totalCaloriesBurned;
        public double avgCaloriesPerDay;
        public long totalDurationMinutes;
        public long avgDurationPerDay;
        public int recordCount;
        public double[] dailyCalories;
        public long[] dailyDuration;
        public String[] dates;
        
        public ExerciseStats(int days) {
            this.dailyCalories = new double[days];
            this.dailyDuration = new long[days];
            this.dates = new String[days];
        }
    }
    
    public static class SleepStats {
        public double avgDurationHours;
        public double avgQualityScore;
        public int totalWakeUpCount;
        public double avgWakeUpCount;
        public int recordCount;
        public double[] dailyDuration;
        public double[] dailyQuality;
        public String[] dates;
        
        public SleepStats(int days) {
            this.dailyDuration = new double[days];
            this.dailyQuality = new double[days];
            this.dates = new String[days];
        }
    }
    
    public static class ComprehensiveStats {
        public DietStats dietStats;
        public ExerciseStats exerciseStats;
        public SleepStats sleepStats;
        public Date startDate;
        public Date endDate;
        public String periodType;
        
        public ComprehensiveStats(Date startDate, Date endDate, String periodType) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.periodType = periodType;
        }
    }
    
    public static DietStats calculateDietStats(List<DietRecord> records, Date startDate, Date endDate) {
        long daysDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        int days = (int) Math.min(daysDiff, 31);
        DietStats stats = new DietStats(days);
        
        stats.recordCount = records.size();
        
        for (DietRecord record : records) {
            stats.totalCalories += record.getCalories();
            stats.totalProtein += record.getProtein();
            stats.totalCarbs += record.getCarbs();
            stats.totalFat += record.getFat();
        }
        
        if (days > 0) {
            stats.avgCaloriesPerDay = stats.totalCalories / days;
        }
        
        return stats;
    }
    
    public static ExerciseStats calculateExerciseStats(List<ExerciseRecord> records, Date startDate, Date endDate) {
        long daysDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        int days = (int) Math.min(daysDiff, 31);
        ExerciseStats stats = new ExerciseStats(days);
        
        stats.recordCount = records.size();
        
        for (ExerciseRecord record : records) {
            stats.totalCaloriesBurned += record.getCaloriesBurned();
            stats.totalDurationMinutes += record.getDurationMinutes();
        }
        
        if (days > 0) {
            stats.avgCaloriesPerDay = stats.totalCaloriesBurned / days;
            stats.avgDurationPerDay = stats.totalDurationMinutes / days;
        }
        
        return stats;
    }
    
    public static SleepStats calculateSleepStats(List<SleepRecord> records, Date startDate, Date endDate) {
        long daysDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        int days = (int) Math.min(daysDiff, 31);
        SleepStats stats = new SleepStats(days);
        
        stats.recordCount = records.size();
        
        double totalDuration = 0;
        double totalQuality = 0;
        
        for (SleepRecord record : records) {
            double durationHours = record.getDurationHours();
            int qualityScore = record.getSleepQuality() != null ? record.getSleepQuality().getScore() : 0;
            
            totalDuration += durationHours;
            totalQuality += qualityScore;
            stats.totalWakeUpCount += record.getWakeUpCount();
        }
        
        if (stats.recordCount > 0) {
            stats.avgDurationHours = totalDuration / stats.recordCount;
            stats.avgQualityScore = totalQuality / stats.recordCount;
            stats.avgWakeUpCount = (double) stats.totalWakeUpCount / stats.recordCount;
        }
        
        return stats;
    }
    
    public static ComprehensiveStats generateComprehensiveStats(
            List<DietRecord> dietRecords,
            List<ExerciseRecord> exerciseRecords,
            List<SleepRecord> sleepRecords,
            Date startDate,
            Date endDate,
            String periodType) {
        
        ComprehensiveStats stats = new ComprehensiveStats(startDate, endDate, periodType);
        stats.dietStats = calculateDietStats(dietRecords, startDate, endDate);
        stats.exerciseStats = calculateExerciseStats(exerciseRecords, startDate, endDate);
        stats.sleepStats = calculateSleepStats(sleepRecords, startDate, endDate);
        
        return stats;
    }
    
    public static String getQualityDescription(double avgScore) {
        if (avgScore >= 4.5) return "优秀";
        if (avgScore >= 3.5) return "良好";
        if (avgScore >= 2.5) return "一般";
        if (avgScore >= 1.5) return "较差";
        return "很差";
    }
    
    public static String getSleepSuggestion(double avgHours) {
        if (avgHours < 6) {
            return "睡眠时间不足，建议增加睡眠时长至7-8小时";
        } else if (avgHours > 9) {
            return "睡眠时间过长，建议保持7-8小时的规律睡眠";
        } else {
            return "睡眠时间适中，请继续保持";
        }
    }
    
    public static String getExerciseSuggestion(double avgCalories, long avgMinutes) {
        if (avgMinutes < 30) {
            return "运动量偏少，建议每天至少运动30分钟";
        } else if (avgCalories < 200) {
            return "运动强度偏低，可适当增加运动强度";
        } else {
            return "运动量适中，请继续保持";
        }
    }
    
    public static String getDietSuggestion(double avgCalories) {
        if (avgCalories < 1200) {
            return "日均热量摄入偏低，请注意营养均衡";
        } else if (avgCalories > 2500) {
            return "日均热量摄入偏高，建议控制饮食";
        } else {
            return "热量摄入适中，请继续保持";
        }
    }
}
