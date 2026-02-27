package com.example.timedisplay.health.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.ExerciseRecord;

import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {

    private final HealthDatabase database;
    private final LiveData<List<ExerciseRecord>> allRecords;

    public ExerciseViewModel(@NonNull Application application) {
        super(application);
        database = HealthDatabase.getDatabase(application);
        allRecords = database.healthDao().getAllExerciseRecords();
    }

    public LiveData<List<ExerciseRecord>> getAllRecords() {
        return allRecords;
    }

    public void insert(ExerciseRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().insertExerciseRecord(record);
        });
    }

    public void update(ExerciseRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().updateExerciseRecord(record);
        });
    }

    public void delete(ExerciseRecord record) {
        HealthDatabase.databaseWriteExecutor.execute(() -> {
            database.healthDao().deleteExerciseRecord(record);
        });
    }
}
