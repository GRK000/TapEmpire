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

public class WorldAdapter extends RecyclerView.Adapter<WorldAdapter.WorldViewHolder> {

    public interface OnWorldClickListener {
        void onWorldClick(int position, World world);
    }

    private final List<World> worlds;
    private final GameState state;
    private final OnWorldClickListener listener;

    public WorldAdapter(List<World> worlds, GameState state, OnWorldClickListener listener) {
        this.worlds = worlds;
        this.state = state;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_world_pro, parent, false);
        return new WorldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorldViewHolder holder, int position) {
        World world = worlds.get(position);
        World.WorldTheme theme = world.getTheme();

        holder.tvEmoji.setText(theme.emoji);
        holder.tvName.setText(theme.name);
        holder.tvDescription.setText(theme.description);
        holder.tvMultiplier.setText("⚡ x" + String.format("%.2f", world.getProductionMultiplier()) + " producción");

        // Bonuses especiales
        StringBuilder bonuses = new StringBuilder();
        for (String bonus : world.getSpecialBonuses()) {
            if (bonuses.length() > 0) bonuses.append(" • ");
            bonuses.append("✨ ").append(bonus);
        }
        holder.tvBonuses.setText(bonuses.toString());

        boolean isCurrent = position == state.getCurrentWorldIndex();

        if (world.isUnlocked()) {
            holder.itemView.setAlpha(1.0f);
            holder.tvRequirements.setVisibility(View.GONE);

            if (isCurrent) {
                holder.btnAction.setText("✓ ACTIVO");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundResource(R.drawable.btn_success);
            } else {
                holder.btnAction.setText("VIAJAR");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundResource(R.drawable.btn_primary);
            }
        } else {
            holder.itemView.setAlpha(0.6f);

            String reqs = "🔒 " + GameState.fmt(world.getUnlockCost()) + " coins";
            if (world.getRequiredPrestigeLevel() > 0) {
                reqs += " • Prestigio " + world.getRequiredPrestigeLevel();
            }
            holder.tvRequirements.setVisibility(View.VISIBLE);
            holder.tvRequirements.setText(reqs);

            if (world.canUnlock(state.getCoins(), state.getPrestigeLevel())) {
                holder.btnAction.setText("💰 COMPRAR");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundResource(R.drawable.btn_gold);
            } else {
                holder.btnAction.setText("🔒 BLOQUEADO");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundResource(R.drawable.btn_disabled);
            }
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorldClick(position, world);
            }
        });
    }

    @Override
    public int getItemCount() {
        return worlds.size();
    }

    static class WorldViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDescription, tvMultiplier, tvBonuses, tvRequirements;
        Button btnAction;

        WorldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMultiplier = itemView.findViewById(R.id.tvMultiplier);
            tvBonuses = itemView.findViewById(R.id.tvBonuses);
            tvRequirements = itemView.findViewById(R.id.tvRequirements);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
