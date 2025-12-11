package com.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.data.local.AppDatabase;
import com.app.data.local.dao.HabitDao;
import com.app.data.local.entity.HabitEntity;
import com.app.util.Resource;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private final HabitDao habitDao;
    private final ExecutorService executorService;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<HabitEntity>> getAllActiveHabits() {
        return habitDao.getAllActiveHabits();
    }

    public LiveData<HabitEntity> getHabitById(long habitId) {
        return habitDao.getHabitById(habitId);
    }

    public LiveData<Resource<Long>> insertHabit(String name, String description, String frequency) {
        MutableLiveData<Resource<Long>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                HabitEntity habit = new HabitEntity();
                habit.setName(name);
                habit.setDescription(description);
                habit.setFrequency(frequency);

                long id = habitDao.insert(habit);
                result.postValue(Resource.success(id));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> updateHabit(long habitId, String name, String description, String frequency) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                HabitEntity habit = habitDao.getHabitById(habitId).getValue();
                if (habit != null) {
                    habit.setName(name);
                    habit.setDescription(description);
                    habit.setFrequency(frequency);
                    habit.setUpdatedAt(new Date());

                    habitDao.update(habit);
                    result.postValue(Resource.success(true));
                } else {
                    result.postValue(Resource.error("Habit not found", false));
                }
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), false));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> deleteHabit(long habitId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                habitDao.softDelete(habitId, new Date().getTime());
                result.postValue(Resource.success(true));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), false));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> updateStreak(long habitId, int currentStreak, int bestStreak) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                habitDao.updateStreak(habitId, currentStreak, bestStreak, new Date().getTime());
                result.postValue(Resource.success(true));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), false));
            }
        });

        return result;
    }
}
