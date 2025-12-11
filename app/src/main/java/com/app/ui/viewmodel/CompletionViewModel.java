package com.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.app.data.local.entity.HabitCompletionEntity;
import com.app.data.repository.CompletionRepository;
import com.app.util.Resource;

import java.util.List;

public class CompletionViewModel extends AndroidViewModel {
    private final CompletionRepository completionRepository;

    public CompletionViewModel(@NonNull Application application) {
        super(application);
        this.completionRepository = new CompletionRepository(application);
    }

    public LiveData<List<HabitCompletionEntity>> getHabitCompletions(long habitId, int limit) {
        return completionRepository.getHabitCompletions(habitId, limit);
    }

    public LiveData<HabitCompletionEntity> getTodayCompletion(long habitId) {
        return completionRepository.getTodayCompletion(habitId);
    }

    public LiveData<Resource<Long>> markAsCompleted(long habitId, String status, int progress, String details) {
        return completionRepository.markAsCompleted(habitId, status, progress, details);
    }

    public LiveData<Integer> getCompletedCount(long habitId) {
        return completionRepository.getCompletedCount(habitId);
    }
}