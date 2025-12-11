package com.app.ui.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.app.R;
import com.app.notification.NotificationHelper;
import com.app.notification.NotificationScheduler;
import com.app.ui.viewmodel.SettingsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private SwitchMaterial switchDailyReminders;
    private AutoCompleteTextView actvReminderTime;
    private MaterialButton btnSave;
    private SettingsViewModel settingsViewModel;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this);

        // Configurar launcher para permisos
        setupPermissionLauncher();

        initViews();
        setupViewModel();
        setupTimeDropdown();
        setupListeners();
        loadSettings();
    }

    private void setupPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Permisos de notificación concedidos",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permisos de notificación denegados",
                                Toast.LENGTH_LONG).show();
                        switchDailyReminders.setChecked(false);
                    }
                }
        );
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchDailyReminders = findViewById(R.id.switchDailyReminders);
        actvReminderTime = findViewById(R.id.actvReminderTime);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupViewModel() {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    private void setupTimeDropdown() {
        String[] times = {
                "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM",
                "11:00 AM", "12:00 PM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM",
                "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM",
                "09:00 PM", "10:00 PM"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, times);
        actvReminderTime.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestNotificationPermission();
            }
        });

        btnSave.setOnClickListener(v -> handleSave());
    }

    private void loadSettings() {
        settingsViewModel.getSettings().observe(this, settings -> {
            if (settings != null) {
                switchDailyReminders.setChecked(settings.isDailyRemindersEnabled());
                actvReminderTime.setText(formatTime(settings.getReminderTime()), false);
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void handleSave() {
        boolean remindersEnabled = switchDailyReminders.isChecked();
        String reminderTime = parseTime(actvReminderTime.getText().toString());

        // Verificar permisos antes de guardar
        if (remindersEnabled && !NotificationHelper.areNotificationsEnabled(this)) {
            Toast.makeText(this, "Por favor, habilita los permisos de notificación",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnSave.setEnabled(false);

        settingsViewModel.updateSettings(remindersEnabled, reminderTime)
                .observe(this, resource -> {
                    if (resource != null) {
                        switch (resource.getStatus()) {
                            case SUCCESS:
                                // Programar o cancelar notificaciones
                                if (remindersEnabled) {
                                    NotificationScheduler.scheduleNotification(this, reminderTime);
                                    Toast.makeText(this, "Notificaciones programadas para " +
                                            formatTime(reminderTime), Toast.LENGTH_SHORT).show();
                                } else {
                                    NotificationScheduler.cancelNotification(this);
                                    Toast.makeText(this, "Notificaciones deshabilitadas",
                                            Toast.LENGTH_SHORT).show();
                                }
                                finish();
                                break;
                            case ERROR:
                                btnSave.setEnabled(true);
                                Toast.makeText(this, "Error: " + resource.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case LOADING:
                                break;
                        }
                    }
                });
    }

    private String formatTime(String time24) {
        try {
            String[] parts = time24.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String amPm = hour >= 12 ? "PM" : "AM";
            if (hour > 12) hour -= 12;
            if (hour == 0) hour = 12;

            return String.format("%02d:%02d %s", hour, minute, amPm);
        } catch (Exception e) {
            return time24;
        }
    }

    private String parseTime(String time12) {
        try {
            String[] parts = time12.split(" ");
            String[] timeParts = parts[0].split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            String amPm = parts[1];

            if (amPm.equals("PM") && hour != 12) hour += 12;
            if (amPm.equals("AM") && hour == 12) hour = 0;

            return String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            return "09:00";
        }
    }
}