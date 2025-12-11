package com.app.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.data.local.entity.HabitEntity;
import com.app.data.repository.HabitRepository;
import com.app.util.Resource;

import java.util.List;

public class HabitViewModel extends AndroidViewModel {
    private final HabitRepository habitRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final LiveData<List<HabitEntity>> allActiveHabits;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        this.habitRepository = new HabitRepository(application);
        this.allActiveHabits = habitRepository.getAllActiveHabits();
    }

    public LiveData<List<HabitEntity>> getAllActiveHabits() {
        return allActiveHabits;
    }

    public LiveData<HabitEntity> getHabitById(long habitId) {
        return habitRepository.getHabitById(habitId);
    }

    public LiveData<Resource<Long>> createHabit(String name, String description, String frequency) {
        if (!validateHabitName(name)) {
            errorMessage.setValue("Habit name is required");
            MutableLiveData<Resource<Long>> result = new MutableLiveData<>();
            result.setValue(Resource.error("Habit name is required", null));
            return result;
        }
        if (!validateFrequency(frequency)) {
            errorMessage.setValue("Please select a frequency");
            MutableLiveData<Resource<Long>> result = new MutableLiveData<>();
            result.setValue(Resource.error("Please select a frequency", null));
            return result;
        }
        return habitRepository.insertHabit(name, description, frequency);
    }

    public LiveData<Resource<Boolean>> updateHabit(long habitId, String name, String description, String frequency) {
        if (!validateHabitName(name)) {
            errorMessage.setValue("Habit name is required");
            MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
            result.setValue(Resource.error("Habit name is required", false));
            return result;
        }
        if (!validateFrequency(frequency)) {
            errorMessage.setValue("Please select a frequency");
            MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
            result.setValue(Resource.error("Please select a frequency", false));
            return result;
        }
        return habitRepository.updateHabit(habitId, name, description, frequency);
    }

    public LiveData<Resource<Boolean>> deleteHabit(long habitId) {
        return habitRepository.deleteHabit(habitId);
    }

    public LiveData<Resource<Boolean>> updateStreak(long habitId, int currentStreak, int bestStreak) {
        return habitRepository.updateStreak(habitId, currentStreak, bestStreak);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    private boolean validateHabitName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private boolean validateFrequency(String frequency) {
        return frequency != null && !frequency.trim().isEmpty();
    }
}
