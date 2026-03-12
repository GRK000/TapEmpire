package com.example.juego;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PetsActivity extends AppCompatActivity {

    private RecyclerView petsRecycler;
    private TextView tvActivePet, tvCoins;
    private PetAdapter adapter;
    private GameState state = GameState.getInstance();
    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pets_pro);

        tvActivePet = findViewById(R.id.tvActivePet);
        tvCoins = findViewById(R.id.tvCoins);
        petsRecycler = findViewById(R.id.petsRecycler);

        petsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetAdapter(state.getPets(), state, new PetAdapter.OnPetActionListener() {
            @Override
            public void onBuyClick(int position, Pet pet) {
                if (state.buyPet(position)) {
                    adapter.notifyDataSetChanged();
                    updateHeader();
                    Toast.makeText(PetsActivity.this,
                        "🎉 ¡Compraste " + pet.getType().name + "!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PetsActivity.this,
                        "❌ Necesitas " + GameState.fmt(pet.getPurchaseCost()) + " coins",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSelectClick(int position, Pet pet) {
                state.setActivePet(position);
                adapter.notifyDataSetChanged();
                updateHeader();
                Toast.makeText(PetsActivity.this,
                    pet.getType().emoji + " " + pet.getType().name + " ahora es tu mascota activa!",
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFeedClick(int position, Pet pet) {
                if (state.feedPet()) {
                    adapter.notifyItemChanged(position);
                    updateHeader();
                    Toast.makeText(PetsActivity.this,
                        "🍖 " + pet.getType().name + " está feliz! ❤️",
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PetsActivity.this,
                        "❌ No tienes suficientes coins para alimentar",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPetClick(int position, Pet pet) {
                if (state.petPet()) {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(PetsActivity.this,
                        "💕 Le diste cariño a " + pet.getType().name + "!",
                        Toast.LENGTH_SHORT).show();
                } else {
                    long cooldown = pet.getPetCooldownRemaining();
                    Toast.makeText(PetsActivity.this,
                        "⏱️ Espera " + (cooldown / 60) + "m para acariciar de nuevo",
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
        petsRecycler.setAdapter(adapter);

        updateHeader();
        startUpdateLoop();
    }

    private void updateHeader() {
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));

        Pet active = state.getActivePet();
        if (active != null && active.isOwned()) {
            double bonus = active.getCurrentBonus() * 100;
            String status = active.isAlive() ? active.getMood().emoji : (active.isGhost() ? "👻" : "💀");
            tvActivePet.setText(active.getType().emoji + " " + active.getType().name +
                " " + status + " +" + String.format("%.1f", bonus) + "%");
        } else {
            tvActivePet.setText("Sin mascota activa");
        }
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = () -> {
            state.updateProduction(System.currentTimeMillis());
            updateHeader();
            updateHandler.postDelayed(updateRunnable, 1000);
        };
        updateHandler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateHandler != null) updateHandler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdateLoop();
    }
}
