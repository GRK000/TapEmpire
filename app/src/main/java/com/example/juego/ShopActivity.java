package com.example.juego;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShopActivity extends AppCompatActivity {

    private RecyclerView upgradesRecycler;
    private TextView tvCoins;
    private UpgradeAdapter adapter;
    private GameState state = GameState.getInstance();

    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_pro);

        tvCoins = findViewById(R.id.tvCoins);
        upgradesRecycler = findViewById(R.id.upgradesRecycler);

        // Configurar RecyclerView
        upgradesRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UpgradeAdapter(state.getUpgrades(), state, (position, upgrade) -> {
            if (upgrade.isMaxLevel()) {
                Toast.makeText(this, "⭐ ¡Mejora al máximo!", Toast.LENGTH_SHORT).show();
            } else if (state.buyUpgrade(position)) {
                adapter.notifyItemChanged(position);
                updateHeader();
                Toast.makeText(this, "✅ " + upgrade.getName() + " mejorado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ No tienes suficientes coins", Toast.LENGTH_SHORT).show();
            }
        });
        upgradesRecycler.setAdapter(adapter);

        updateHeader();
        startUpdateLoop();
    }

    private void updateHeader() {
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                state.updateProduction(System.currentTimeMillis());
                updateHeader();
                adapter.notifyDataSetChanged();
                updateHandler.postDelayed(this, 500);
            }
        };
        updateHandler.post(updateRunnable);
    }

    private void stopUpdateLoop() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdateLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdateLoop();
    }
}

