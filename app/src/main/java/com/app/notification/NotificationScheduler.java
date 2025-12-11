package com.app.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private static final int REQUEST_CODE = 100;

    /**
     * Programa una notificación diaria
     * @param context Contexto de la aplicación
     * @param time Hora en formato "HH:mm" (ejemplo: "09:00")
     */
    public static void scheduleNotification(Context context, String time) {
        try {
            // Parsear la hora
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Configurar el calendario para la hora especificada
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Si la hora ya pasó hoy, programar para mañana
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Crear el intent para el receiver
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.app.DAILY_REMINDER");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            // Obtener el AlarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                // Cancelar alarmas anteriores
                alarmManager.cancel(pendingIntent);

                // Programar nueva alarma
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d(TAG, "Notification scheduled for: " + calendar.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }

    /**
     * Cancela todas las notificaciones programadas
     */
    public static void cancelNotification(Context context) {
        try {
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction("com.app.DAILY_REMINDER");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                Log.d(TAG, "Notification cancelled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notification", e);
        }
    }
}
