package com.app.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.R;
import com.app.notification.NotificationHelper;
import com.app.ui.adapter.MainHabitAdapter;
import com.app.ui.viewmodel.CompletionViewModel;
import com.app.ui.viewmodel.HabitViewModel;
import com.app.util.Resource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvHabits;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAdd;
    private CircularProgressIndicator progressCircleMain;
    private TextView tvProgressMain, tvMotivation, tvMotivationDetail;
    private HabitViewModel habitViewModel;
    private CompletionViewModel completionViewModel;
    private MainHabitAdapter habitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.createNotificationChannel(this);

        initViews();
        setupViewModels();
        setupRecyclerView();
        setupListeners();
        observeHabits();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar progreso cuando volvemos a la actividad
        habitViewModel.getAllActiveHabits().observe(this, habits -> {
            if (habits != null && !habits.isEmpty()) {
                calculateAndUpdateProgress(habits.size());
            }
        });
    }

    private void initViews() {
        rvHabits = findViewById(R.id.rvHabits);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAdd = findViewById(R.id.fabAdd);
        progressCircleMain = findViewById(R.id.progressCircleMain);
        tvProgressMain = findViewById(R.id.tvProgressMain);
        tvMotivation = findViewById(R.id.tvMotivation);
        tvMotivationDetail = findViewById(R.id.tvMotivationDetail);

        // Configurar progress indicator
        progressCircleMain.setMax(100);
    }

    private void setupViewModels() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        completionViewModel = new ViewModelProvider(this).get(CompletionViewModel.class);
    }

    private void setupRecyclerView() {
        habitAdapter = new MainHabitAdapter(habit -> {
            Intent intent = new Intent(MainActivity.this, DetailsHabitActivity.class);
            intent.putExtra("habitId", habit.getId());
            startActivity(intent);
        }, this::toggleHabitCompletion);

        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        rvHabits.setAdapter(habitAdapter);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void observeHabits() {
        habitViewModel.getAllActiveHabits().observe(this, habits -> {
            if (habits != null && !habits.isEmpty()) {
                rvHabits.setVisibility(View.VISIBLE);
                llEmptyState.setVisibility(View.GONE);
                habitAdapter.submitList(habits);
                calculateAndUpdateProgress(habits.size());
            } else {
                rvHabits.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
                updateProgressUI(0, 0);
            }
        });
    }

    private void calculateAndUpdateProgress(int totalHabits) {
        if (totalHabits == 0) {
            updateProgressUI(0, 0);
            return;
        }

        // Usar AtomicInteger para contar de forma segura
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger checkedHabits = new AtomicInteger(0);

        habitViewModel.getAllActiveHabits().observeForever(habits -> {
            if (habits == null || habits.isEmpty()) return;

            // Resetear contadores
            completedCount.set(0);
            checkedHabits.set(0);

            for (int i = 0; i < habits.size(); i++) {
                long habitId = habits.get(i).getId();

                completionViewModel.getTodayCompletion(habitId).observeForever(completion -> {
                    if (completion != null && completion.getStatus().equals("completed")) {
                        completedCount.incrementAndGet();
                    }

                    // Incrementar el contador de hábitos verificados
                    int checked = checkedHabits.incrementAndGet();

                    // Solo actualizar UI cuando todos los hábitos hayan sido verificados
                    if (checked == habits.size()) {
                        updateProgressUI(completedCount.get(), habits.size());
                    }
                });
            }
        });
    }

    private void updateProgressUI(int completed, int total) {
        runOnUiThread(() -> {
            int percentage = total > 0 ? (completed * 100) / total : 0;
            progressCircleMain.setProgressCompat(percentage, true);
            tvProgressMain.setText(completed + "/" + total);

            if (total == 0) {
                tvMotivation.setText("¡Comencemos!");
                tvMotivationDetail.setText("Crea tu primer hábito.");
            } else if (completed == 0) {
                tvMotivation.setText("¡Empieza tu día!");
                tvMotivationDetail.setText("Completa tu primer hábito.");
            } else if (completed == total) {
                tvMotivation.setText("¡Increíble progreso!");
                tvMotivationDetail.setText("Todos los hábitos completados hoy.");
            } else {
                tvMotivation.setText("¡Avanza!");
                tvMotivationDetail.setText(completed + " de " + total + " hábitos hechos.");
            }
        });
    }

    private void toggleHabitCompletion(long habitId) {
        completionViewModel.getTodayCompletion(habitId).observe(this, existing -> {
            if (existing != null && existing.getStatus().equals("completed")) {
                Intent intent = new Intent(MainActivity.this, DetailsHabitActivity.class);
                intent.putExtra("habitId", habitId);
                startActivity(intent);
            } else {
                completionViewModel.markAsCompleted(habitId, "completed", 100, "Completado desde main")
                        .observe(this, resource -> {
                            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                                // Recalcular progreso después de marcar como completado
                                habitViewModel.getAllActiveHabits().observe(this, habits -> {
                                    if (habits != null) {
                                        calculateAndUpdateProgress(habits.size());
                                    }
                                });
                            }
                        });
            }
        });
    }
}