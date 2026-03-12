package com.example.juego;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MoreActivity extends AppCompatActivity {

    private GameState state = GameState.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_pro);

        setupClickListeners();
        updateStatuses();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBusinessMap).setOnClickListener(v ->
            startActivity(new Intent(this, BusinessMapActivity.class)));

        findViewById(R.id.btnUpgrades).setOnClickListener(v ->
            startActivity(new Intent(this, ShopActivity.class)));

        findViewById(R.id.btnMissions).setOnClickListener(v ->
            startActivity(new Intent(this, MissionsActivity.class)));

        findViewById(R.id.btnPets).setOnClickListener(v ->
            startActivity(new Intent(this, PetsActivity.class)));

        findViewById(R.id.btnAchievements).setOnClickListener(v ->
            startActivity(new Intent(this, AchievementsActivity.class)));

        findViewById(R.id.btnPrestige).setOnClickListener(v ->
            startActivity(new Intent(this, PrestigeActivity.class)));

        findViewById(R.id.btnStats).setOnClickListener(v -> showStatsDialog());
    }

    private void updateStatuses() {
        // Misiones
        int completedMissions = 0;
        for (DailyMission m : state.getDailyMissions()) {
            if (m.isCompleted()) completedMissions++;
        }
        ((TextView) findViewById(R.id.tvMissionsStatus))
            .setText(completedMissions + "/" + state.getDailyMissions().size() + " completadas");

        // Mascotas
        int ownedPets = 0;
        for (Pet p : state.getPets()) {
            if (p.isOwned()) ownedPets++;
        }
        String petText = ownedPets > 0 ? ownedPets + " mascotas" : "Compañeros con bonus";
        ((TextView) findViewById(R.id.tvPetsStatus)).setText(petText);

        // Logros
        int unlockedAchievements = 0;
        for (Achievement a : state.getAchievements()) {
            if (a.isUnlocked()) unlockedAchievements++;
        }
        ((TextView) findViewById(R.id.tvAchievementsStatus))
            .setText(unlockedAchievements + "/" + state.getAchievements().size() + " desbloqueados");

        // Prestigio
        ((TextView) findViewById(R.id.tvPrestigeStatus))
            .setText("Nivel " + state.getPrestigeLevel() + " - x" +
                     String.format("%.2f", state.getPrestigeMultiplier()));
    }

    private void showStatsDialog() {
        String stats = "📊 ESTADÍSTICAS\n\n" +
            "💰 Coins totales ganadas: " + GameState.fmt(state.getTotalCoinsEarned()) + "\n" +
            "👆 Taps totales: " + GameState.fmt(state.getTotalTaps()) + "\n" +
            "💥 Críticos totales: " + GameState.fmt(state.getTotalCriticalHits()) + "\n" +
            "⚙️ Producción actual: " + GameState.fmt(state.getProductionPerSecond()) + "/seg\n" +
            "🏭 Generadores comprados: " + state.getTotalGeneratorsBought() + "\n" +
            "🎮 Minijuegos jugados: " + state.getTotalMiniGamesPlayed() + "\n" +
            "🌟 Eventos completados: " + state.getTotalEventsCompleted() + "\n" +
            "⭐ Nivel de prestigio: " + state.getPrestigeLevel() + "\n" +
            "🔥 Combo máximo: " + state.getComboSystem().getMaxCombo() + "x";

        new AlertDialog.Builder(this)
            .setTitle("📊 Tus Estadísticas")
            .setMessage(stats)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatuses();
    }
}
