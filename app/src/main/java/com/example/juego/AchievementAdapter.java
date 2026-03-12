package com.example.juego;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final List<Achievement> achievements;
    private final GameState state;

    public AchievementAdapter(List<Achievement> achievements, GameState state) {
        this.achievements = achievements;
        this.state = state;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement_pro, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.tvEmoji.setText(achievement.getEmoji());
        holder.tvName.setText(achievement.getName());
        holder.tvDescription.setText(achievement.getDescription());
        holder.tvReward.setText("💰 " + GameState.fmt(achievement.getReward()));

        // Calcular progreso
        double currentValue = getCurrentValue(achievement);
        double targetValue = achievement.getTargetValue();
        int progressPercent = (int) Math.min(100, (currentValue / targetValue) * 100);

        if (achievement.isUnlocked()) {
            holder.tvStatus.setText("✓");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success));
            holder.itemView.setAlpha(1.0f);
            holder.tvReward.setText("✓ Reclamado");
            holder.tvReward.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.success));
        } else {
            holder.tvStatus.setText(progressPercent + "%");
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.itemView.setAlpha(0.7f);
            holder.tvReward.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.coins_gold));
        }
    }

    private double getCurrentValue(Achievement achievement) {
        switch (achievement.getType()) {
            case TOTAL_TAPS:
                return state.getTotalTaps();
            case TOTAL_COINS_EARNED:
                return state.getTotalCoinsEarned();
            case COINS_OWNED:
                return state.getCoins();
            case PRODUCTION_PER_SECOND:
                return state.getProductionPerSecond();
            case PRESTIGE_LEVEL:
                return state.getPrestigeLevel();
            case MAX_COMBO:
                return state.getMaxComboReached();
            default:
                return 0;
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDescription, tvStatus, tvReward;

        AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReward = itemView.findViewById(R.id.tvReward);
        }
    }
}
