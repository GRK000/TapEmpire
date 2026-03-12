package com.example.juego;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PrestigeActivity extends AppCompatActivity {

    private TextView tvCurrentLevel, tvCurrentMultiplier;
    private TextView tvNextMultiplier, tvCoinsNeeded;
    private Button btnPrestige;
    private GameState state = GameState.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prestige_pro);

        // Vincular vistas
        tvCurrentLevel = findViewById(R.id.tvCurrentLevel);
        tvCurrentMultiplier = findViewById(R.id.tvCurrentMultiplier);
        tvNextMultiplier = findViewById(R.id.tvNextMultiplier);
        tvCoinsNeeded = findViewById(R.id.tvCoinsNeeded);
        btnPrestige = findViewById(R.id.btnPrestige);

        // Configurar botón
        btnPrestige.setOnClickListener(v -> attemptPrestige());

        updateUI();
    }

    private void updateUI() {
        // Stats actuales
        tvCurrentLevel.setText("Nivel Actual: " + state.getPrestigeLevel());
        tvCurrentMultiplier.setText("Multiplicador: x" + String.format("%.2f", state.getPrestigeMultiplier()));

        // Nuevo multiplicador si hace prestigio
        if (state.canPrestige()) {
            double newMultiplier = 1 + (state.getPrestigeLevel() + 1) * 0.05;
            tvNextMultiplier.setText("+x" + String.format("%.2f", newMultiplier - state.getPrestigeMultiplier()));
            tvNextMultiplier.setTextColor(getResources().getColor(R.color.success, null));
            tvCoinsNeeded.setText("✅ ¡Listo para prestigio!");
            tvCoinsNeeded.setTextColor(getResources().getColor(R.color.success, null));
            btnPrestige.setEnabled(true);
            btnPrestige.setAlpha(1.0f);
            btnPrestige.setBackgroundResource(R.drawable.btn_gold);
        } else {
            double requirement = 1_000_000 * Math.pow(10, state.getPrestigeLevel());
            tvNextMultiplier.setText("+x0.05");
            tvNextMultiplier.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tvCoinsNeeded.setText("Necesitas: " + GameState.fmt(requirement) + " totales\n" +
                    "Tienes: " + GameState.fmt(state.getTotalCoinsEarned()));
            tvCoinsNeeded.setTextColor(getResources().getColor(R.color.warning, null));
            btnPrestige.setEnabled(false);
            btnPrestige.setAlpha(0.5f);
            btnPrestige.setBackgroundResource(R.drawable.btn_disabled);
        }
    }

    private void attemptPrestige() {
        if (!state.canPrestige()) {
            return;
        }

        double points = state.getPrestigePointsAvailable();
        double newMultiplier = 1 + (state.getPrestigeLevel() + 1) * 0.05;

        new AlertDialog.Builder(this)
                .setTitle("⭐ Confirmar Prestigio ⭐")
                .setMessage("¿Estás seguro?\n\n" +
                        "Obtendrás:\n" +
                        "• +" + GameState.fmt(points) + " Puntos de Prestigio\n" +
                        "• Multiplicador x" + String.format("%.2f", newMultiplier) + "\n\n" +
                        "Perderás:\n" +
                        "• Todas tus coins\n" +
                        "• Todos tus generadores\n" +
                        "• Todas tus mejoras")
                .setPositiveButton("¡PRESTIGIAR!", (dialog, which) -> {
                    state.performPrestige();

                    new AlertDialog.Builder(this)
                            .setTitle("🎉 ¡Prestigio Completado! 🎉")
                            .setMessage("Has alcanzado el nivel de prestigio " + state.getPrestigeLevel() + "!\n\n" +
                                    "Tu nuevo multiplicador: x" + String.format("%.2f", state.getPrestigeMultiplier()))
                            .setPositiveButton("¡A por más!", (d, w) -> {
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}
