package com.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.app.R;
import com.app.data.local.entity.HabitEntity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainHabitAdapter extends ListAdapter<HabitEntity, MainHabitAdapter.HabitViewHolder> {
    private final OnHabitClickListener clickListener;
    private final OnCheckClickListener checkListener;

    public interface OnHabitClickListener {
        void onHabitClick(HabitEntity habit);
    }

    public interface OnCheckClickListener {
        void onCheckClick(long habitId);
    }

    public MainHabitAdapter(OnHabitClickListener clickListener, OnCheckClickListener checkListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.checkListener = checkListener;
    }

    private static final DiffUtil.ItemCallback<HabitEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HabitEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull HabitEntity oldItem, @NonNull HabitEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull HabitEntity oldItem, @NonNull HabitEntity newItem) {
                    return oldItem.getName().equals(newItem.getName());
                }
            };

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_main, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitEntity habit = getItem(position);
        holder.bind(habit, clickListener, checkListener);
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardHabit;
        private final CardView cvIcon;
        private final ImageView ivHabitIcon;
        private final TextView tvHabitName;
        private final TextView tvDescription;
        private final FloatingActionButton fabCheck;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            cardHabit = itemView.findViewById(R.id.cardHabit);
            cvIcon = itemView.findViewById(R.id.cvIcon);
            ivHabitIcon = itemView.findViewById(R.id.ivHabitIcon);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            fabCheck = itemView.findViewById(R.id.fabCheck);
        }

        public void bind(HabitEntity habit, OnHabitClickListener clickListener,
                         OnCheckClickListener checkListener) {
            tvHabitName.setText(habit.getName());
            tvDescription.setText(habit.getDescription());

            // Valores por defecto (asegúrate de agregar estos recursos en drawable / color)
            int bgColorRes = R.color.green_light;
            int iconColor = R.color.primary;
            int iconRes = R.drawable.ic_spa_outli;
            String name = habit.getName() != null ? habit.getName().toLowerCase().trim() : "";

            if (name.contains("correr")) {
                bgColorRes = R.color.blue_light_3;
                iconColor = R.color.accent_blue;
                iconRes = R.drawable.ic_run;
            } else if (name.contains("leer")) {
                bgColorRes = R.color.purple_light_4;
                iconColor = R.color.accent_purple;
                iconRes = R.drawable.ic_read;
            } else if (name.contains("tomar agua") || name.contains("agua")) {
                bgColorRes = R.color.cyan_light_4;
                iconColor = R.color.accent_blue;
                iconRes = R.drawable.ic_water_drop;
            } else if (name.contains("meditar")) {
                bgColorRes = R.color.pink_light_4;
                iconColor = R.color.accent_green;
                iconRes = R.drawable.ic_meditate;
            }

            // Aplicar color y icono
            ivHabitIcon.setImageResource(iconRes);
            ivHabitIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), iconColor));
            cvIcon.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), bgColorRes));

            cardHabit.setOnClickListener(v -> clickListener.onHabitClick(habit));
            fabCheck.setOnClickListener(v -> checkListener.onCheckClick(habit.getId()));
        }
    }
}
