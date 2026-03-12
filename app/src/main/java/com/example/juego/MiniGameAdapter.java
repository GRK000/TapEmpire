package com.example.juego;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MiniGameAdapter extends RecyclerView.Adapter<MiniGameAdapter.MiniGameViewHolder> {

    public interface OnMiniGameClickListener {
        void onPlayClick(int position, MiniGame miniGame);
    }

    private final List<MiniGame> miniGames;
    private final OnMiniGameClickListener listener;

    public MiniGameAdapter(List<MiniGame> miniGames, OnMiniGameClickListener listener) {
        this.miniGames = miniGames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MiniGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_minigame_pro, parent, false);
        return new MiniGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniGameViewHolder holder, int position) {
        MiniGame game = miniGames.get(position);
        MiniGame.MiniGameType type = game.getType();

        holder.tvEmoji.setText(type.emoji);
        holder.tvName.setText(type.name);
        holder.tvDescription.setText(type.description);

        int remaining = game.getRemainingPlays();
        int max = game.getMaxPlaysPerDay();
        holder.tvPlaysRemaining.setText("🎯 " + remaining + "/" + max + " partidas disponibles");

        if (remaining <= 0) {
            holder.tvPlaysRemaining.setTextColor(Color.parseColor("#F44336"));
        } else {
            holder.tvPlaysRemaining.setTextColor(Color.parseColor("#4CAF50"));
        }

        if (game.isAvailable()) {
            holder.btnPlay.setText("🎮 JUGAR");
            holder.btnPlay.setEnabled(true);
            holder.btnPlay.setBackgroundResource(R.drawable.btn_primary);
            holder.tvCooldown.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);
        } else if (remaining <= 0) {
            holder.btnPlay.setText("😴 MAÑANA");
            holder.btnPlay.setEnabled(false);
            holder.btnPlay.setBackgroundResource(R.drawable.btn_disabled);
            holder.tvCooldown.setVisibility(View.VISIBLE);
            holder.tvCooldown.setText("Sin partidas. Vuelve mañana.");
            holder.itemView.setAlpha(0.6f);
        } else {
            long cooldown = game.getCooldownRemaining();
            long mins = cooldown / 60;
            long secs = cooldown % 60;
            holder.btnPlay.setText("⏱️ " + mins + ":" + String.format("%02d", secs));
            holder.btnPlay.setEnabled(false);
            holder.btnPlay.setBackgroundResource(R.drawable.btn_gold);
            holder.tvCooldown.setVisibility(View.VISIBLE);
            holder.tvCooldown.setText("Cooldown activo");
            holder.itemView.setAlpha(0.8f);
        }

        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayClick(position, game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return miniGames.size();
    }

    static class MiniGameViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDescription, tvPlaysRemaining, tvCooldown;
        Button btnPlay;

        MiniGameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPlaysRemaining = itemView.findViewById(R.id.tvPlaysRemaining);
            tvCooldown = itemView.findViewById(R.id.tvCooldown);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}
