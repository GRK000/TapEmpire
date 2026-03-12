package com.example.juego;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GeneratorsActivity extends AppCompatActivity {

    private RecyclerView generatorsRecycler;
    private TextView tvCoinsHeader, tvProductionHeader;
    private Button btnViewMap;
    private GeneratorAdapter adapter;
    private GameState state = GameState.getInstance();

    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generators_pro);

        tvCoinsHeader = findViewById(R.id.tvCoinsHeader);
        tvProductionHeader = findViewById(R.id.tvProductionHeader);
        btnViewMap = findViewById(R.id.btnViewMap);
        generatorsRecycler = findViewById(R.id.generatorsRecycler);

        // Botón para ver mapa
        btnViewMap.setOnClickListener(v ->
            startActivity(new Intent(this, BusinessMapActivity.class)));

        // Configurar RecyclerView
        generatorsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GeneratorAdapter(state.getGenerators(), state, (position, generator) -> {
            if (state.buyGenerator(position)) {
                adapter.notifyItemChanged(position);
                updateHeader();
                Toast.makeText(this, "✅ " + generator.getName() + " comprado!", Toast.LENGTH_SHORT).show();
            } else if (!generator.isUnlocked()) {
                Toast.makeText(this, "🔒 Necesitas " + GameState.fmt(generator.getUnlockRequirement()) +
                        " coins totales para desbloquear", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ No tienes suficientes coins", Toast.LENGTH_SHORT).show();
            }
        });
        generatorsRecycler.setAdapter(adapter);

        updateHeader();
        startUpdateLoop();
    }

    private void updateHeader() {
        tvCoinsHeader.setText("💰 " + GameState.fmt(state.getCoins()));
        tvProductionHeader.setText("⚙️ Producción: " + GameState.fmt(state.getProductionPerSecond()) + "/seg");
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
