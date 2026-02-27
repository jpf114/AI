package com.example.timedisplay.health.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.timedisplay.health.database.HealthDatabase;
import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.SleepRecord;

import java.util.Date;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final HealthDatabase database;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        database = HealthDatabase.getDatabase(application);
    }

    public LiveData<List<DietRecord>> getDietRecords(Date startDate, Date endDate) {
        return database.healthDao().getDietRecordsBetween(startDate, endDate);
    }

    public LiveData<List<ExerciseRecord>> getExerciseRecords(Date startDate, Date endDate) {
        return database.healthDao().getExerciseRecordsBetween(startDate, endDate);
    }

    public LiveData<List<SleepRecord>> getSleepRecords(Date startDate, Date endDate) {
        return database.healthDao().getSleepRecordsBetween(startDate, endDate);
    }
}
