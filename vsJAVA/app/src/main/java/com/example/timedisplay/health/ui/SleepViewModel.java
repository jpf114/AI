package com.example.timedisplay.health.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.SleepRecord;

import java.util.List;

public class SleepViewModel extends AndroidViewModel {

    private final HealthDatabase database;
    private final LiveData<List<SleepRecord>> allRecords;

    public SleepViewModel(@NonNull Application application) {
        super(application);
        database = HealthDatabase.getDatabase(application);
        allRecords = database.healthDao().getAllSleepRecords();
    }

    public LiveData<List<SleepRecord>> getAllRecords() {
        return allRecords;
    }

    public void insert(SleepRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().insertSleepRecord(record);
        });
    }

    public void update(SleepRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().updateSleepRecord(record);
        });
    }

    public void delete(SleepRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().deleteSleepRecord(record);
        });
    }
}
