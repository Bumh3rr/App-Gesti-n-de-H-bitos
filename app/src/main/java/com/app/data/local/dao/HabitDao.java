package com.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.app.data.local.entity.HabitEntity;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    long insert(HabitEntity habit);

    @Update
    void update(HabitEntity habit);

    @Query("DELETE FROM habits WHERE id = :habitId")
    void delete(long habitId);

    @Query("UPDATE habits SET isActive = 0, updatedAt = :updatedAt WHERE id = :habitId")
    void softDelete(long habitId, long updatedAt);

    @Query("SELECT * FROM habits WHERE id = :habitId")
    LiveData<HabitEntity> getHabitById(long habitId);

    // Método síncrono para operaciones en background
    @Query("SELECT * FROM habits WHERE id = :habitId")
    HabitEntity getHabitByIdSync(long habitId);

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    LiveData<List<HabitEntity>> getAllActiveHabits();

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    LiveData<List<HabitEntity>> getAllHabits();

    // Método síncrono para notificaciones
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    List<HabitEntity> getAllActiveHabitsSync();

    @Query("UPDATE habits SET currentStreak = :currentStreak, bestStreak = :bestStreak, updatedAt = :updatedAt WHERE id = :habitId")
    void updateStreak(long habitId, int currentStreak, int bestStreak, long updatedAt);

    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    LiveData<Integer> getActiveHabitsCount();
}