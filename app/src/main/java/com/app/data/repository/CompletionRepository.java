package com.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.app.data.local.AppDatabase;
import com.app.data.local.dao.HabitCompletionDao;
import com.app.data.local.entity.HabitCompletionEntity;
import com.app.util.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletionRepository {
    private final HabitCompletionDao completionDao;
    private final ExecutorService executorService;
    private final SimpleDateFormat dateFormat;

    public CompletionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        completionDao = db.habitCompletionDao();
        executorService = Executors.newSingleThreadExecutor();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public LiveData<List<HabitCompletionEntity>> getHabitCompletions(long habitId, int limit) {
        if (limit > 0) {
            return completionDao.getHabitCompletions(habitId, limit);
        } else {
            return completionDao.getAllHabitCompletions(habitId);
        }
    }

    public LiveData<HabitCompletionEntity> getTodayCompletion(long habitId) {
        String today = dateFormat.format(new Date());
        return completionDao.getCompletionByDate(habitId, today);
    }

    public LiveData<Resource<Long>> markAsCompleted(long habitId, String status, int progress, String details) {
        MutableLiveData<Resource<Long>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                String today = dateFormat.format(new Date());

                HabitCompletionEntity existing = completionDao.getCompletionByDateSync(habitId, today);

                if (existing != null) {
                    existing.setStatus(status);
                    existing.setProgress(progress);
                    existing.setDetails(details);
                    existing.setCompletedAt(new Date());
                    completionDao.update(existing);
                    result.postValue(Resource.success(existing.getId()));
                } else {
                    HabitCompletionEntity completion = new HabitCompletionEntity();
                    completion.setHabitId(habitId);
                    completion.setDate(today);
                    completion.setStatus(status);
                    completion.setProgress(progress);
                    completion.setDetails(details);

                    long id = completionDao.insert(completion);
                    result.postValue(Resource.success(id));
                }
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Integer> getCompletedCount(long habitId) {
        return completionDao.getCompletedCount(habitId);
    }
}