package com.example.juego;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WorldsActivity extends AppCompatActivity {

    private RecyclerView worldsRecycler;
    private TextView tvCurrentWorld, tvCoins;
    private WorldAdapter adapter;
    private GameState state = GameState.getInstance();

    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worlds_pro);

        tvCurrentWorld = findViewById(R.id.tvCurrentWorld);
        tvCoins = findViewById(R.id.tvCoins);
        worldsRecycler = findViewById(R.id.worldsRecycler);

        worldsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorldAdapter(state.getWorlds(), state, new WorldAdapter.OnWorldClickListener() {
            @Override
            public void onWorldClick(int position, World world) {
                if (world.isUnlocked()) {
                    if (state.switchWorld(position)) {
                        adapter.notifyDataSetChanged();
                        updateHeader();
                        Toast.makeText(WorldsActivity.this,
                            "🌍 Viajaste a " + world.getTheme().name, Toast.LENGTH_SHORT).show();
                    }
                } else if (world.canUnlock(state.getCoins(), state.getPrestigeLevel())) {
                    if (state.buyWorld(position)) {
                        adapter.notifyDataSetChanged();
                        updateHeader();
                        Toast.makeText(WorldsActivity.this,
                            "🎉 ¡Desbloqueaste " + world.getTheme().name + "!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = "🔒 Necesitas: " + GameState.fmt(world.getUnlockCost()) + " coins";
                    if (world.getRequiredPrestigeLevel() > state.getPrestigeLevel()) {
                        msg += " y Prestigio " + world.getRequiredPrestigeLevel();
                    }
                    Toast.makeText(WorldsActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        worldsRecycler.setAdapter(adapter);

        updateHeader();
        startUpdateLoop();
    }

    private void updateHeader() {
        World current = state.getCurrentWorld();
        tvCurrentWorld.setText(current.getTheme().emoji + " " + current.getTheme().name +
            " (x" + String.format("%.2f", current.getProductionMultiplier()) + ")");
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = () -> {
            state.updateProduction(System.currentTimeMillis());
            updateHeader();
            adapter.notifyDataSetChanged();
            updateHandler.postDelayed(updateRunnable, 500);
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
