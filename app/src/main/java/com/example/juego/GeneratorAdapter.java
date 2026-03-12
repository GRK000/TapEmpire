package com.example.juego;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GeneratorAdapter extends RecyclerView.Adapter<GeneratorAdapter.GeneratorViewHolder> {

    public interface OnGeneratorClickListener {
        void onBuyClick(int position, Generator generator);
    }

    private final List<Generator> generators;
    private final GameState state;
    private final OnGeneratorClickListener listener;

    public GeneratorAdapter(List<Generator> generators, GameState state, OnGeneratorClickListener listener) {
        this.generators = generators;
        this.state = state;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GeneratorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_generator_pro, parent, false);
        return new GeneratorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneratorViewHolder holder, int position) {
        Generator gen = generators.get(position);

        holder.tvEmoji.setText(gen.getEmoji());
        holder.tvName.setText(gen.getName().replace(gen.getEmoji() + " ", ""));
        holder.tvOwned.setText("x" + gen.getOwned());
        holder.tvProduction.setText(GameState.fmt(gen.getProductionPerSecond()) + "/s");

        double cost = gen.getCurrentCost();
        holder.btnBuy.setText("💰 " + GameState.fmt(cost));

        // Estado del generador
        if (!gen.isUnlocked()) {
            holder.itemView.setAlpha(0.5f);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("🔒 " + GameState.fmt(gen.getUnlockRequirement()) + " totales");
            holder.btnBuy.setEnabled(false);
            holder.btnBuy.setBackgroundResource(R.drawable.btn_disabled);
        } else if (state.getCoins() < cost) {
            holder.itemView.setAlpha(0.8f);
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnBuy.setEnabled(false);
            holder.btnBuy.setBackgroundResource(R.drawable.btn_disabled);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.tvStatus.setVisibility(View.GONE);
            holder.btnBuy.setEnabled(true);
            holder.btnBuy.setBackgroundResource(R.drawable.btn_gold);
        }

        holder.btnBuy.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBuyClick(position, gen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return generators.size();
    }

    static class GeneratorViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvOwned, tvProduction, tvStatus;
        Button btnBuy;

        GeneratorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvOwned = itemView.findViewById(R.id.tvOwned);
            tvProduction = itemView.findViewById(R.id.tvProduction);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }
}
