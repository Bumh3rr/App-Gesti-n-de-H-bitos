package com.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.app.data.local.entity.SettingsEntity;

@Dao
public interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SettingsEntity settings);

    @Update
    void update(SettingsEntity settings);

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    LiveData<SettingsEntity> getSettings();

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    SettingsEntity getSettingsSync();

    @Query("UPDATE settings SET dailyRemindersEnabled = :enabled, updatedAt = :updatedAt WHERE id = 1")
    void updateDailyReminders(boolean enabled, long updatedAt);

    @Query("UPDATE settings SET reminderTime = :time, updatedAt = :updatedAt WHERE id = 1")
    void updateReminderTime(String time, long updatedAt);
}
