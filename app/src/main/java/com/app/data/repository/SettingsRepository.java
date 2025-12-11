package com.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.app.data.local.AppDatabase;
import com.app.data.local.dao.SettingsDao;
import com.app.data.local.entity.SettingsEntity;
import com.app.util.Resource;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsRepository {
    private final SettingsDao settingsDao;
    private final ExecutorService executorService;

    public SettingsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        settingsDao = db.settingsDao();
        executorService = Executors.newSingleThreadExecutor();

        // Inicializar settings si no existe
        executorService.execute(() -> {
            SettingsEntity existing = settingsDao.getSettingsSync();
            if (existing == null) {
                settingsDao.insert(new SettingsEntity());
            }
        });
    }

    public LiveData<SettingsEntity> getSettings() {
        return settingsDao.getSettings();
    }

    public LiveData<Resource<Boolean>> updateSettings(boolean dailyRemindersEnabled, String reminderTime) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executorService.execute(() -> {
            try {
                SettingsEntity settings = settingsDao.getSettingsSync();
                if (settings == null) {
                    settings = new SettingsEntity();
                }

                settings.setDailyRemindersEnabled(dailyRemindersEnabled);
                settings.setReminderTime(reminderTime);
                settings.setUpdatedAt(new Date());

                settingsDao.update(settings);
                result.postValue(Resource.success(true));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage(), false));
            }
        });

        return result;
    }
}