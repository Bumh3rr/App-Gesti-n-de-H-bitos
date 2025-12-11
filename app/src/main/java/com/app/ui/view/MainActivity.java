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

    private void initViews() {
        rvHabits = findViewById(R.id.rvHabits);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAdd = findViewById(R.id.fabAdd);
        progressCircleMain = findViewById(R.id.progressCircleMain);
        tvProgressMain = findViewById(R.id.tvProgressMain);
        tvMotivation = findViewById(R.id.tvMotivation);
        tvMotivationDetail = findViewById(R.id.tvMotivationDetail);
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
                updateProgress(habits.size());
            } else {
                rvHabits.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
                updateProgress(0);
            }
        });
    }

    private void updateProgress(int totalHabits) {
        if (totalHabits == 0) {
            progressCircleMain.setProgress(0);
            tvProgressMain.setText("0/0");
            tvMotivation.setText("¡Comencemos!");
            tvMotivationDetail.setText("Crea tu primer hábito.");
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Contar hábitos completados hoy
        int[] completedCount = {0};

        habitViewModel.getAllActiveHabits().observe(this, habits -> {
            if (habits == null) return;

            for (int i = 0; i < habits.size(); i++) {
                long habitId = habits.get(i).getId();
                completionViewModel.getTodayCompletion(habitId).observe(this, completion -> {
                    if (completion != null && completion.getStatus().equals("completed")) {
                        completedCount[0]++;
                    }
                    updateProgressUI(completedCount[0], totalHabits);
                });
            }
        });
    }

    private void updateProgressUI(int completed, int total) {
        int percentage = total > 0 ? (completed * 100) / total : 0;
        progressCircleMain.setProgress(percentage);
        tvProgressMain.setText(completed + "/" + total);

        if (completed == 0) {
            tvMotivation.setText("¡Empieza tu día!");
            tvMotivationDetail.setText("Completa tu primer hábito.");
        } else if (completed == total) {
            tvMotivation.setText("¡Increíble progreso!");
            tvMotivationDetail.setText("Todos los hábitos completados hoy.");
        } else {
            tvMotivation.setText("¡Avanza!");
            tvMotivationDetail.setText(completed + " de " + total + " hábitos hechos.");
        }
    }

    private void toggleHabitCompletion(long habitId) {
        completionViewModel.getTodayCompletion(habitId).observe(this, existing -> {
            if (existing != null && existing.getStatus().equals("completed")) {
                // Ya está completado, no hacer nada o mostrar detalles
                Intent intent = new Intent(MainActivity.this, DetailsHabitActivity.class);
                intent.putExtra("habitId", habitId);
                startActivity(intent);
            } else {
                // Marcar como completado
                completionViewModel.markAsCompleted(habitId, "completed", 100, "Completed from main")
                        .observe(this, resource -> {
                            if (resource != null && resource.getStatus() ==
                                    Resource.Status.SUCCESS) {
                                updateProgress(habitAdapter.getItemCount());
                            }
                        });
            }
        });
    }
}