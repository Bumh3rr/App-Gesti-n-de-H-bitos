package com.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.app.data.local.entity.SettingsEntity;
import com.app.data.repository.SettingsRepository;
import com.app.util.Resource;

public class SettingsViewModel extends AndroidViewModel {
    private final SettingsRepository settingsRepository;
    private final LiveData<SettingsEntity> settings;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.settingsRepository = new SettingsRepository(application);
        this.settings = settingsRepository.getSettings();
    }

    public LiveData<SettingsEntity> getSettings() {
        return settings;
    }

    public LiveData<Resource<Boolean>> updateSettings(boolean dailyRemindersEnabled, String reminderTime) {
        return settingsRepository.updateSettings(dailyRemindersEnabled, reminderTime);
    }
}
