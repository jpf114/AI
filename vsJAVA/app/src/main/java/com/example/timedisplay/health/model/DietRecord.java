package com.example.timedisplay.health.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timedisplay.health.database.DateConverter;

import java.util.Date;

@Entity(tableName = "diet_records")
@TypeConverters(DateConverter.class)
public class DietRecord {
    
    public enum MealType {
        BREAKFAST("早餐"),
        LUNCH("午餐"),
        DINNER("晚餐"),
        SNACK("加餐");
        
        private final String displayName;
        
        MealType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String foodName;
    private MealType mealType;
    private Date intakeTime;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private double amount;
    private String unit;
    private String note;
    private Date createdAt;
    private Date updatedAt;
    
    public DietRecord() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public MealType getMealType() {
        return mealType;
    }
    
    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
    
    public Date getIntakeTime() {
        return intakeTime;
    }
    
    public void setIntakeTime(Date intakeTime) {
        this.intakeTime = intakeTime;
    }
    
    public double getCalories() {
        return calories;
    }
    
    public void setCalories(double calories) {
        this.calories = calories;
    }
    
    public double getProtein() {
        return protein;
    }
    
    public void setProtein(double protein) {
        this.protein = protein;
    }
    
    public double getCarbs() {
        return carbs;
    }
    
    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }
    
    public double getFat() {
        return fat;
    }
    
    public void setFat(double fat) {
        this.fat = fat;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
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
    
    public String getMealTypeDisplay() {
        return mealType != null ? mealType.getDisplayName() : "";
    }
}
