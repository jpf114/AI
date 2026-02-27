package com.example.timedisplay.health.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.timedisplay.health.model.DietRecord;
import com.example.timedisplay.health.model.ExerciseRecord;
import com.example.timedisplay.health.model.FoodItem;
import com.example.timedisplay.health.model.SleepRecord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        DietRecord.class,
        ExerciseRecord.class,
        SleepRecord.class,
        FoodItem.class
}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class HealthDatabase extends RoomDatabase {

    public abstract HealthDao healthDao();

    private static volatile HealthDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static HealthDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HealthDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    HealthDatabase.class, "health_database")
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                HealthDao dao = INSTANCE.healthDao();
                FoodItem[] defaultFoods = FoodItem.getDefaultFoods();
                for (FoodItem food : defaultFoods) {
                    dao.insertFoodItem(food);
                }
            });
        }
    };
}
