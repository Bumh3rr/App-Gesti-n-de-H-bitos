package com.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "settings")
public class SettingsEntity {
    @PrimaryKey
    private int id = 1; // Solo habrá un registro de configuración
    private boolean dailyRemindersEnabled;
    private String reminderTime; // Format: HH:mm
    private Date updatedAt;

    public SettingsEntity() {
        this.dailyRemindersEnabled = true;
        this.reminderTime = "09:00";
        this.updatedAt = new Date();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isDailyRemindersEnabled() { return dailyRemindersEnabled; }
    public void setDailyRemindersEnabled(boolean dailyRemindersEnabled) {
        this.dailyRemindersEnabled = dailyRemindersEnabled;
    }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
