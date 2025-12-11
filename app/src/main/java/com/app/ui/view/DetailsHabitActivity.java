package com.app.ui.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.R;
import com.app.data.local.entity.HabitEntity;
import com.app.ui.adapter.HistoryAdapter;
import com.app.ui.viewmodel.CompletionViewModel;
import com.app.ui.viewmodel.HabitViewModel;
import com.app.util.Resource;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DetailsHabitActivity extends AppCompatActivity {
    private ImageButton btnBack, btnEdit, btnDelete;
    private TextView tvHabitName, tvDescription, tvFrequency, tvProgress, tvCurrentStreak, tvBestStreak;
    private CardView cvDescription;
    private CircularProgressIndicator progressCircle;
    private RecyclerView rvHistory;
    private MaterialButton btnMarkComplete;
    private HabitViewModel habitViewModel;
    private CompletionViewModel completionViewModel;
    private HistoryAdapter historyAdapter;
    private long habitId;
    private HabitEntity currentHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_habit);

        habitId = getIntent().getLongExtra("habitId", -1);
        if (habitId == -1) {
            finish();
            return;
        }

        initViews();
        setupViewModels();
        setupRecyclerView();
        setupListeners();
        loadHabitData();
        loadHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        tvHabitName = findViewById(R.id.tvHabitName);
        tvDescription = findViewById(R.id.tvDescription);
        tvFrequency = findViewById(R.id.tvFrequency);
        cvDescription = findViewById(R.id.cvDescription);
        tvProgress = findViewById(R.id.tvProgress);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        tvBestStreak = findViewById(R.id.tvBestStreak);
        progressCircle = findViewById(R.id.progressCircle);
        rvHistory = findViewById(R.id.rvHistory);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);

        // Configurar el CircularProgressIndicator
        progressCircle.setMax(100);
        progressCircle.setProgress(0);
    }

    private void setupViewModels() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        completionViewModel = new ViewModelProvider(this).get(CompletionViewModel.class);
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsHabitActivity.this, AddHabitActivity.class);
            intent.putExtra("habitId", habitId);
            startActivity(intent);
        });

        btnMarkComplete.setOnClickListener(v -> showCompletionDialog());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void loadHabitData() {
        habitViewModel.getHabitById(habitId).observe(this, habit -> {
            if (habit != null) {
                currentHabit = habit;
                tvHabitName.setText(habit.getName());

                // Mostrar descripción si existe
                if (habit.getDescription() != null && !habit.getDescription().trim().isEmpty()) {
                    tvDescription.setText(habit.getDescription());
                    cvDescription.setVisibility(View.VISIBLE);
                } else {
                    cvDescription.setVisibility(View.GONE);
                }

                // Mostrar frecuencia
                tvFrequency.setText(habit.getFrequency());

                // Mostrar rachas
                tvCurrentStreak.setText(habit.getCurrentStreak() + " días");
                tvBestStreak.setText(habit.getBestStreak() + " días");

                // Calcular progreso
                calculateProgress();
            }
        });
    }

    private void loadHistory() {
        completionViewModel.getHabitCompletions(habitId, 10).observe(this, completions -> {
            if (completions != null) {
                historyAdapter.submitList(completions);
            }
        });
    }

    private void calculateProgress() {
        completionViewModel.getCompletedCount(habitId).observe(this, count -> {
            if (count != null && currentHabit != null) {
                // Calcular días desde creación
                long daysSinceCreation = (System.currentTimeMillis() -
                        currentHabit.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24);

                // Evitar división por cero
                if (daysSinceCreation == 0) {
                    daysSinceCreation = 1;
                }

                int progressPercent = (int) ((count * 100.0) / daysSinceCreation);
                progressPercent = Math.min(progressPercent, 100);

                tvProgress.setText(progressPercent + "%");

                // Usar setProgressCompat para animación suave
                progressCircle.setProgressCompat(progressPercent, true);

                // Log para debug
                android.util.Log.d("DetailsHabit", "Count: " + count +
                        ", Days: " + daysSinceCreation +
                        ", Progress: " + progressPercent + "%");
            }
        });
    }

    private void showCompletionDialog() {
        String[] options = {"Completado (100%)", "Parcial (50%)", "Omitido (0%)"};

        new AlertDialog.Builder(this)
                .setTitle("Marcar como...")
                .setItems(options, (dialog, which) -> {
                    String status;
                    int progress;
                    String details = "";

                    switch (which) {
                        case 0:
                            status = "completed";
                            progress = 100;
                            details = "Completamente completado";
                            break;
                        case 1:
                            status = "partial";
                            progress = 50;
                            details = "Parcialmente completado";
                            break;
                        case 2:
                            status = "skipped";
                            progress = 0;
                            details = "Omitido";
                            break;
                        default:
                            return;
                    }

                    markCompletion(status, progress, details);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void markCompletion(String status, int progress, String details) {
        completionViewModel.markAsCompleted(habitId, status, progress, details)
                .observe(this, resource -> {
                    if (resource != null) {
                        switch (resource.getStatus()) {
                            case SUCCESS:
                                Toast.makeText(this, "Marcado como " + status, Toast.LENGTH_SHORT).show();
                                updateStreak(status);
                                // Recargar historial y progreso
                                loadHistory();
                                calculateProgress();
                                break;
                            case ERROR:
                                Toast.makeText(this, "Error: " + resource.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case LOADING:
                                break;
                        }
                    }
                });
    }

    private void updateStreak(String status) {
        if (currentHabit == null) return;

        int newStreak = currentHabit.getCurrentStreak();

        if (status.equals("completed")) {
            newStreak++;
        } else if (status.equals("skipped")) {
            newStreak = 0;
        }

        int newBestStreak = Math.max(newStreak, currentHabit.getBestStreak());

        habitViewModel.updateStreak(habitId, newStreak, newBestStreak)
                .observe(this, resource -> {
                    if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                        // Streak actualizado
                    }
                });
    }

    private void showDeleteDialog() {
        if (currentHabit == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Eliminar Hábito")
                .setMessage("¿Estás seguro de que quieres eliminar '" + currentHabit.getName() +
                        "'? Esto también eliminará todo el historial de finalización. Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteHabit())
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    private void deleteHabit() {
        habitViewModel.deleteHabit(habitId).observe(this, resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case SUCCESS:
                        Toast.makeText(this, "Hábito eliminado exitosamente",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case ERROR:
                        Toast.makeText(this, "Error: " + resource.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }
}