package com.app.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.app.data.local.dao.HabitCompletionDao;
import com.app.data.local.dao.HabitDao;
import com.app.data.local.dao.SettingsDao;
import com.app.data.local.entity.HabitCompletionEntity;
import com.app.data.local.entity.HabitEntity;
import com.app.data.local.entity.SettingsEntity;

@Database(
        entities = {
                HabitEntity.class,
                HabitCompletionEntity.class,
                SettingsEntity.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract HabitDao habitDao();
    public abstract HabitCompletionDao habitCompletionDao();
    public abstract SettingsDao settingsDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "mindwell_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
