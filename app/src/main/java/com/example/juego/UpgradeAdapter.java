package com.example.juego;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpgradeAdapter extends RecyclerView.Adapter<UpgradeAdapter.UpgradeViewHolder> {

    public interface OnUpgradeClickListener {
        void onBuyClick(int position, Upgrade upgrade);
    }

    private final List<Upgrade> upgrades;
    private final GameState state;
    private final OnUpgradeClickListener listener;

    public UpgradeAdapter(List<Upgrade> upgrades, GameState state, OnUpgradeClickListener listener) {
        this.upgrades = upgrades;
        this.state = state;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UpgradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upgrade_pro, parent, false);
        return new UpgradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpgradeViewHolder holder, int position) {
        Upgrade upgrade = upgrades.get(position);

        holder.tvEmoji.setText(upgrade.getEmoji());
        holder.tvName.setText(upgrade.getName());
        holder.tvDescription.setText(upgrade.getDescription());
        holder.tvLevel.setText("Nv. " + upgrade.getCurrentLevel() + "/" + upgrade.getMaxLevel());

        // Effect text
        if (holder.tvEffect != null) {
            holder.tvEffect.setText("⚡ +" + (int)(upgrade.getEffectValue() * upgrade.getCurrentLevel() * 100) + "%");
        }

        if (upgrade.isMaxLevel()) {
            holder.btnBuy.setText("MAX ⭐");
            holder.btnBuy.setEnabled(false);
            holder.btnBuy.setBackgroundResource(R.drawable.btn_success);
            holder.itemView.setAlpha(0.8f);
        } else {
            double cost = upgrade.getCurrentCost();
            holder.btnBuy.setText("💰 " + GameState.fmt(cost));

            if (state.getCoins() >= cost) {
                holder.btnBuy.setEnabled(true);
                holder.btnBuy.setBackgroundResource(R.drawable.btn_primary);
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.btnBuy.setEnabled(false);
                holder.btnBuy.setBackgroundResource(R.drawable.btn_disabled);
                holder.itemView.setAlpha(0.7f);
            }
        }

        holder.btnBuy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuyClick(position, upgrade);
            }
        });
    }

    @Override
    public int getItemCount() {
        return upgrades.size();
    }

    static class UpgradeViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvDescription, tvLevel, tvEffect;
        Button btnBuy;

        UpgradeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvEffect = itemView.findViewById(R.id.tvEffect);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}
