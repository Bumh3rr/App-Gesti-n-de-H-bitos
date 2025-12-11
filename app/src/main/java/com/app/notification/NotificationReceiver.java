package com.app.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.data.local.AppDatabase;
import com.app.data.local.entity.HabitEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification received");

        // Obtener hábitos activos y mostrar notificación
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<HabitEntity> habits = db.habitDao().getAllActiveHabitsSync();

                if (habits != null && !habits.isEmpty()) {
                    int totalHabits = habits.size();
                    String title = "¡Hora de tus hábitos!";
                    String message = "Tienes " + totalHabits + " hábito(s) pendiente(s) hoy. ¡Vamos!";

                    NotificationHelper.showNotification(context, title, message);
                } else {
                    String title = "¡Buenos días!";
                    String message = "Agrega tus primeros hábitos y comienza tu viaje de bienestar.";

                    NotificationHelper.showNotification(context, title, message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error showing notification", e);
            }
        });
    }
}
