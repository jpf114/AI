package com.example.timedisplay.health.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_items")
public class FoodItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private double caloriesPer100g;
    private double proteinPer100g;
    private double carbsPer100g;
    private double fatPer100g;
    private String category;
    private boolean isCustom;
    private boolean isFavorite;

    public FoodItem() {
    }

    @Ignore
    public FoodItem(String name, double caloriesPer100g, String category) {
        this.name = name;
        this.caloriesPer100g = caloriesPer100g;
        this.category = category;
        this.isCustom = false;
        this.isFavorite = false;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getCaloriesPer100g() {
        return caloriesPer100g;
    }
    
    public void setCaloriesPer100g(double caloriesPer100g) {
        this.caloriesPer100g = caloriesPer100g;
    }
    
    public double getProteinPer100g() {
        return proteinPer100g;
    }
    
    public void setProteinPer100g(double proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }
    
    public double getCarbsPer100g() {
        return carbsPer100g;
    }
    
    public void setCarbsPer100g(double carbsPer100g) {
        this.carbsPer100g = carbsPer100g;
    }
    
    public double getFatPer100g() {
        return fatPer100g;
    }
    
    public void setFatPer100g(double fatPer100g) {
        this.fatPer100g = fatPer100g;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public boolean isCustom() {
        return isCustom;
    }
    
    public void setCustom(boolean custom) {
        isCustom = custom;
    }
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    
    public double calculateCalories(double amount, String unit) {
        if ("g".equals(unit) || "克".equals(unit)) {
            return caloriesPer100g * amount / 100.0;
        } else if ("kg".equals(unit) || "千克".equals(unit)) {
            return caloriesPer100g * amount * 10;
        } else if ("份".equals(unit)) {
            return caloriesPer100g * amount;
        }
        return caloriesPer100g * amount / 100.0;
    }
    
    public static FoodItem[] getDefaultFoods() {
        return new FoodItem[] {
            new FoodItem("米饭", 116, "主食"),
            new FoodItem("面条", 137, "主食"),
            new FoodItem("馒头", 223, "主食"),
            new FoodItem("燕麦", 389, "主食"),
            new FoodItem("鸡蛋", 155, "蛋白质"),
            new FoodItem("鸡胸肉", 165, "蛋白质"),
            new FoodItem("牛肉", 250, "蛋白质"),
            new FoodItem("猪肉", 242, "蛋白质"),
            new FoodItem("鱼肉", 206, "蛋白质"),
            new FoodItem("豆腐", 76, "蛋白质"),
            new FoodItem("牛奶", 54, "饮品"),
            new FoodItem("豆浆", 31, "饮品"),
            new FoodItem("苹果", 52, "水果"),
            new FoodItem("香蕉", 89, "水果"),
            new FoodItem("橙子", 47, "水果"),
            new FoodItem("西红柿", 18, "蔬菜"),
            new FoodItem("黄瓜", 16, "蔬菜"),
            new FoodItem("菠菜", 23, "蔬菜"),
            new FoodItem("西兰花", 34, "蔬菜"),
            new FoodItem("胡萝卜", 41, "蔬菜"),
            new FoodItem("薯片", 536, "零食"),
            new FoodItem("巧克力", 546, "零食"),
            new FoodItem("坚果", 607, "零食"),
            new FoodItem("酸奶", 72, "饮品"),
            new FoodItem("可乐", 42, "饮品")
        };
    }
}
