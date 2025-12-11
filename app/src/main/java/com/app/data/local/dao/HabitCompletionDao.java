package com.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.app.data.local.entity.HabitCompletionEntity;

import java.util.List;

@Dao
public interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(HabitCompletionEntity completion);

    @Update
    void update(HabitCompletionEntity completion);

    @Query("DELETE FROM habit_completions WHERE id = :completionId")
    void delete(long completionId);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC LIMIT :limit")
    LiveData<List<HabitCompletionEntity>> getHabitCompletions(long habitId, int limit);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    LiveData<List<HabitCompletionEntity>> getAllHabitCompletions(long habitId);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    LiveData<HabitCompletionEntity> getCompletionByDate(long habitId, String date);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    HabitCompletionEntity getCompletionByDateSync(long habitId, String date);

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND status = 'completed'")
    LiveData<Integer> getCompletedCount(long habitId);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND status = 'completed' ORDER BY date DESC")
    LiveData<List<HabitCompletionEntity>> getCompletedCompletions(long habitId);

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    void deleteAllForHabit(long habitId);
}
