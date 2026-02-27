package com.example.timedisplay.health.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.FoodItem;

import java.util.List;

public class DietViewModel extends AndroidViewModel {

    private final HealthDatabase database;
    private final LiveData<List<DietRecord>> allRecords;
    private final LiveData<List<FoodItem>> allFoodItems;

    public DietViewModel(@NonNull Application application) {
        super(application);
        database = HealthDatabase.getDatabase(application);
        allRecords = database.healthDao().getAllDietRecords();
        allFoodItems = database.healthDao().getAllFoodItems();
    }

    public LiveData<List<DietRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<List<FoodItem>> getAllFoodItems() {
        return allFoodItems;
    }

    public void insert(DietRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().insertDietRecord(record);
        });
    }

    public void update(DietRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().updateDietRecord(record);
        });
    }

    public void delete(DietRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().deleteDietRecord(record);
        });
    }
}
