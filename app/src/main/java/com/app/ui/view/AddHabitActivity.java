package com.app.ui.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.app.R;
import com.app.ui.viewmodel.HabitViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddHabitActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextInputEditText etHabitName, etDescription;
    private AutoCompleteTextView actvFrequency;
    private MaterialButton btnSave;
    private HabitViewModel habitViewModel;
    private long habitId = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        habitId = getIntent().getLongExtra("habitId", -1);
        isEditMode = habitId != -1;

        initViews();
        setupViewModel();
        setupFrequencyDropdown();
        setupListeners();

        if (isEditMode) {
            loadHabitData();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etHabitName = findViewById(R.id.etHabitName);
        etDescription = findViewById(R.id.etDescription);
        actvFrequency = findViewById(R.id.actvFrequency);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupViewModel() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
    }

    private void setupFrequencyDropdown() {
        String[] frequencies = {"A diario", "3 veces por semana", "Dos veces por semana", "Una vez a la semana", "Sólo fines de semana"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencies);
        actvFrequency.setAdapter(adapter);
        actvFrequency.setText("A diario", false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> handleSave());
    }

    private void loadHabitData() {
        habitViewModel.getHabitById(habitId).observe(this, habit -> {
            if (habit != null) {
                etHabitName.setText(habit.getName());
                etDescription.setText(habit.getDescription());
                actvFrequency.setText(habit.getFrequency(), false);
            }
        });
    }

    private void handleSave() {
        String name = etHabitName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String frequency = actvFrequency.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese el nombre del hábito", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        if (isEditMode) {
            habitViewModel.updateHabit(habitId, name, description, frequency).observe(this, resource -> {
                if (resource != null) {
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            Toast.makeText(this, "Hábito actualizado exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case ERROR:
                            btnSave.setEnabled(true);
                            Toast.makeText(this, "Error: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        } else {
            habitViewModel.createHabit(name, description, frequency).observe(this, resource -> {
                if (resource != null) {
                    switch (resource.getStatus()) {
                        case SUCCESS:
                            Toast.makeText(this, "Hábito creado con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        case ERROR:
                            btnSave.setEnabled(true);
                            Toast.makeText(this, "Error: " + resource.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        }
    }
}