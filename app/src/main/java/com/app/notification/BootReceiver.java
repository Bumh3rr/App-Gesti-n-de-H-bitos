package com.app.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.data.local.AppDatabase;
import com.app.data.local.entity.SettingsEntity;

import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted, rescheduling alarms");

            // Reconfigurar las alarmas después del reinicio
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(context);
                    SettingsEntity settings = db.settingsDao().getSettingsSync();

                    if (settings != null && settings.isDailyRemindersEnabled()) {
                        NotificationScheduler.scheduleNotification(
                                context,
                                settings.getReminderTime()
                        );
                        Log.d(TAG, "Alarms rescheduled successfully");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error rescheduling alarms", e);
                }
            });
        }
    }
}
