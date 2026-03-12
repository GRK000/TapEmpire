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

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    public interface OnPetActionListener {
        void onBuyClick(int position, Pet pet);
        void onSelectClick(int position, Pet pet);
        void onFeedClick(int position, Pet pet);
        void onPetClick(int position, Pet pet);
    }

    private final List<Pet> pets;
    private final GameState state;
    private final OnPetActionListener listener;

    public PetAdapter(List<Pet> pets, GameState state, OnPetActionListener listener) {
        this.pets = pets;
        this.state = state;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet_pro, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        Pet.PetType type = pet.getType();

        holder.tvEmoji.setText(type.emoji);
        holder.tvName.setText(type.name);
        holder.tvDescription.setText(type.description);

        double bonusPercent = type.bonusValue * pet.getLevel() * 100;
        holder.tvBonus.setText("⚡ +" + String.format("%.1f", bonusPercent) + "% " + getBonusTypeName(type.bonusType));

        Pet activePet = state.getActivePet();
        boolean isActive = activePet != null && activePet.getType() == type;

        if (pet.isOwned()) {
            holder.tvPrice.setVisibility(View.GONE);

            if (pet.isAlive()) {
                holder.tvMood.setText(pet.getStatusEmoji());
                // Mostrar estadísticas de salud
                String healthStatus = "Nv." + pet.getLevel() + " ❤️" + pet.getHealth() +
                    "% 😊" + pet.getHappiness() + "% 🍖" + (100 - pet.getHunger()) + "%";
                holder.tvLevel.setText(healthStatus);
                holder.itemView.setAlpha(1.0f);

                // Mostrar enfermedades si hay
                if (!pet.getDiseases().isEmpty()) {
                    StringBuilder diseases = new StringBuilder("🏥 ");
                    for (Pet.Disease d : pet.getDiseases()) {
                        diseases.append(d.emoji);
                    }
                    holder.tvDescription.setText(diseases.toString());
                } else {
                    holder.tvDescription.setText(type.description);
                }
            } else {
                holder.tvMood.setText(pet.isGhost() ? "👻" : "💀");
                holder.tvLevel.setText(pet.isGhost() ? "Fantasma (+10% bonus)" : "Fallecido");
                holder.itemView.setAlpha(0.5f);
            }

            holder.btnSecondary.setVisibility(View.VISIBLE);

            if (!pet.isAlive()) {
                holder.btnAction.setText("REVIVIR");
                holder.btnAction.setEnabled(state.getCoins() >= pet.getReviveCost());
                holder.btnAction.setBackgroundResource(R.drawable.btn_primary);
                holder.btnSecondary.setVisibility(View.GONE);
            } else if (isActive) {
                holder.btnAction.setText("✓ ACTIVO");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundResource(R.drawable.btn_success);
                holder.btnSecondary.setText("🍖 FEED");
                holder.btnSecondary.setOnClickListener(v -> listener.onFeedClick(position, pet));
            } else {
                holder.btnAction.setText("ACTIVAR");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundResource(R.drawable.btn_primary);
                holder.btnAction.setOnClickListener(v -> listener.onSelectClick(position, pet));
                holder.btnSecondary.setVisibility(View.GONE);
            }
        } else {
            holder.tvMood.setText("💤");
            holder.tvLevel.setText("No comprada");
            holder.tvPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("💰 " + GameState.fmt(pet.getPurchaseCost()));
            holder.itemView.setAlpha(0.7f);
            holder.btnSecondary.setVisibility(View.GONE);

            if (state.getCoins() >= pet.getPurchaseCost()) {
                holder.btnAction.setText("COMPRAR");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setBackgroundResource(R.drawable.btn_gold);
            } else {
                holder.btnAction.setText("🔒");
                holder.btnAction.setEnabled(false);
                holder.btnAction.setBackgroundResource(R.drawable.btn_disabled);
            }
            holder.btnAction.setOnClickListener(v -> listener.onBuyClick(position, pet));
        }
    }

    private String getBonusTypeName(Pet.BonusType type) {
        switch (type) {
            case PRODUCTION: return "producción";
            case TAP_POWER: return "tap power";
            case CRIT_CHANCE: return "crit chance";
            case COOLDOWN_REDUCTION: return "cooldown";
            case GLOBAL_MULT: return "global";
            case OFFLINE_BONUS: return "offline";
            case ALL_SMALL: return "todo";
            case MEGA_MULT: return "MEGA";
            default: return "";
        }
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvMood, tvName, tvDescription, tvBonus, tvLevel, tvPrice;
        Button btnAction, btnSecondary;

        PetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvBonus = itemView.findViewById(R.id.tvBonus);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnSecondary = itemView.findViewById(R.id.btnSecondary);
        }
    }
}
