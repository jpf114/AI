package com.example.timedisplay.health.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.FoodItem;
import com.example.timedisplay.health.model.SleepRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface HealthDao {
    
    // Diet Records
    @Insert
    long insertDietRecord(DietRecord record);
    
    @Update
    void updateDietRecord(DietRecord record);
    
    @Delete
    void deleteDietRecord(DietRecord record);
    
    @Query("SELECT * FROM diet_records ORDER BY intakeTime DESC")
    LiveData<List<DietRecord>> getAllDietRecords();
    
    @Query("SELECT * FROM diet_records WHERE intakeTime BETWEEN :startDate AND :endDate ORDER BY intakeTime DESC")
    LiveData<List<DietRecord>> getDietRecordsBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM diet_records WHERE intakeTime BETWEEN :startDate AND :endDate ORDER BY intakeTime DESC")
    List<DietRecord> getDietRecordsBetweenSync(Date startDate, Date endDate);
    
    @Query("SELECT SUM(calories) FROM diet_records WHERE intakeTime BETWEEN :startDate AND :endDate")
    double getTotalCaloriesBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM diet_records WHERE id = :id")
    DietRecord getDietRecordById(long id);
    
    // Exercise Records
    @Insert
    long insertExerciseRecord(ExerciseRecord record);
    
    @Update
    void updateExerciseRecord(ExerciseRecord record);
    
    @Delete
    void deleteExerciseRecord(ExerciseRecord record);
    
    @Query("SELECT * FROM exercise_records ORDER BY startTime DESC")
    LiveData<List<ExerciseRecord>> getAllExerciseRecords();
    
    @Query("SELECT * FROM exercise_records WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    LiveData<List<ExerciseRecord>> getExerciseRecordsBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM exercise_records WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    List<ExerciseRecord> getExerciseRecordsBetweenSync(Date startDate, Date endDate);
    
    @Query("SELECT SUM(caloriesBurned) FROM exercise_records WHERE startTime BETWEEN :startDate AND :endDate")
    double getTotalCaloriesBurnedBetween(Date startDate, Date endDate);
    
    @Query("SELECT SUM(durationMinutes) FROM exercise_records WHERE startTime BETWEEN :startDate AND :endDate")
    long getTotalExerciseDurationBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM exercise_records WHERE id = :id")
    ExerciseRecord getExerciseRecordById(long id);
    
    // Sleep Records
    @Insert
    long insertSleepRecord(SleepRecord record);
    
    @Update
    void updateSleepRecord(SleepRecord record);
    
    @Delete
    void deleteSleepRecord(SleepRecord record);
    
    @Query("SELECT * FROM sleep_records ORDER BY recordDate DESC")
    LiveData<List<SleepRecord>> getAllSleepRecords();
    
    @Query("SELECT * FROM sleep_records WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    LiveData<List<SleepRecord>> getSleepRecordsBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM sleep_records WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    List<SleepRecord> getSleepRecordsBetweenSync(Date startDate, Date endDate);
    
    @Query("SELECT AVG(durationMinutes) FROM sleep_records WHERE recordDate BETWEEN :startDate AND :endDate")
    double getAverageSleepDurationBetween(Date startDate, Date endDate);
    
    @Query("SELECT AVG(CASE sleepQuality WHEN 'EXCELLENT' THEN 5 WHEN 'GOOD' THEN 4 WHEN 'FAIR' THEN 3 WHEN 'POOR' THEN 2 WHEN 'VERY_POOR' THEN 1 END) FROM sleep_records WHERE recordDate BETWEEN :startDate AND :endDate")
    double getAverageSleepQualityBetween(Date startDate, Date endDate);
    
    @Query("SELECT * FROM sleep_records WHERE id = :id")
    SleepRecord getSleepRecordById(long id);
    
    // Food Items
    @Insert
    long insertFoodItem(FoodItem foodItem);
    
    @Update
    void updateFoodItem(FoodItem foodItem);
    
    @Delete
    void deleteFoodItem(FoodItem foodItem);
    
    @Query("SELECT * FROM food_items ORDER BY name ASC")
    LiveData<List<FoodItem>> getAllFoodItems();
    
    @Query("SELECT * FROM food_items WHERE isFavorite = 1 ORDER BY name ASC")
    LiveData<List<FoodItem>> getFavoriteFoodItems();
    
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<FoodItem>> searchFoodItems(String query);
    
    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY name ASC")
    LiveData<List<FoodItem>> getFoodItemsByCategory(String category);
    
    @Query("SELECT * FROM food_items WHERE id = :id")
    FoodItem getFoodItemById(long id);
    
    @Query("SELECT COUNT(*) FROM food_items")
    int getFoodItemCount();
}
