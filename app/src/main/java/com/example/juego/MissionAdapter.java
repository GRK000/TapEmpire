package com.example.juego;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    public interface OnMissionClickListener {
        void onClaimClick(int position, DailyMission mission);
    }

    private final List<DailyMission> missions;
    private final GameState state;
    private final OnMissionClickListener listener;

    public MissionAdapter(List<DailyMission> missions, GameState state, OnMissionClickListener listener) {
        this.missions = missions;
        this.state = state;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission_pro, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        DailyMission mission = missions.get(position);

        holder.tvEmoji.setText(mission.getType().emoji);
        holder.tvDescription.setText(mission.getDescription());
        holder.tvDifficulty.setText(mission.getDifficultyStars());
        holder.tvReward.setText("💰 " + GameState.fmt(mission.getCoinReward()));

        int progress = (int) mission.getProgressPercent();
        holder.progressBar.setProgress(progress);
        holder.tvProgress.setText(GameState.fmt(mission.getCurrentProgress()) + "/" +
                                  GameState.fmt(mission.getTargetValue()));

        if (mission.isClaimed()) {
            holder.btnClaim.setText("✓ HECHO");
            holder.btnClaim.setEnabled(false);
            holder.btnClaim.setBackgroundResource(R.drawable.btn_success);
            holder.btnClaim.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(0.6f);
        } else if (mission.isCompleted()) {
            holder.btnClaim.setText("RECLAMAR");
            holder.btnClaim.setEnabled(true);
            holder.btnClaim.setBackgroundResource(R.drawable.btn_gold);
            holder.btnClaim.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.btnClaim.setText(progress + "%");
            holder.btnClaim.setEnabled(false);
            holder.btnClaim.setBackgroundResource(R.drawable.btn_disabled);
            holder.btnClaim.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
        }

        holder.btnClaim.setOnClickListener(v -> {
            if (listener != null && mission.isCompleted() && !mission.isClaimed()) {
                listener.onClaimClick(position, mission);
            }
        });
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvDescription, tvDifficulty, tvProgress, tvReward;
        ProgressBar progressBar;
        Button btnClaim;

        MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvReward = itemView.findViewById(R.id.tvReward);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }
    }
}
