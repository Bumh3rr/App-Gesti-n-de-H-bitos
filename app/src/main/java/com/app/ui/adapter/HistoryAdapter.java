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
import com.app.data.local.entity.HabitCompletionEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<HabitCompletionEntity, HistoryAdapter.HistoryViewHolder> {

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<HabitCompletionEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<HabitCompletionEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull HabitCompletionEntity oldItem,
                                               @NonNull HabitCompletionEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull HabitCompletionEntity oldItem,
                                                  @NonNull HabitCompletionEntity newItem) {
                    return oldItem.getStatus().equals(newItem.getStatus()) &&
                            oldItem.getProgress() == newItem.getProgress();
                }
            };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HabitCompletionEntity completion = getItem(position);
        holder.bind(completion);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardStatusBg;
        private final ImageView ivStatus;
        private final TextView tvStatus;
        private final TextView tvDetail;
        private final TextView tvDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardStatusBg = itemView.findViewById(R.id.cardStatusBg);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(HabitCompletionEntity completion) {
            tvStatus.setText(capitalize(completion.getStatus()));
            tvDetail.setText(completion.getDetails());
            tvDate.setText(formatDate(completion.getDate()));

            int iconRes, bgColor, iconColor;

            switch (completion.getStatus()) {
                case "completed":
                    iconRes = R.drawable.ic_check;
                    bgColor = R.color.green_light;
                    iconColor = R.color.primary;
                    break;
                case "partial":
                    iconRes = R.drawable.ic_water_drop;
                    bgColor = R.color.warning_light;
                    iconColor = R.color.warning;
                    break;
                case "skipped":
                    iconRes = R.drawable.ic_close;
                    bgColor = R.color.error_light;
                    iconColor = R.color.error;
                    break;
                default:
                    iconRes = R.drawable.ic_check;
                    bgColor = R.color.green_light;
                    iconColor = R.color.primary;
                    break;
            }

            ivStatus.setImageResource(iconRes);
            ivStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(), iconColor));
            cardStatusBg.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), bgColor));
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);

                if (date != null) {
                    long diff = System.currentTimeMillis() - date.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);

                    if (days == 0) return "Hoy";
                    if (days == 1) return "Ayer";

                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return dateStr;
        }
    }
}
