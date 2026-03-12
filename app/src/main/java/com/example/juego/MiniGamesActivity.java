package com.example.juego;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Random;

public class MiniGamesActivity extends AppCompatActivity {

    private RecyclerView miniGamesRecycler;
    private TextView tvCoins;
    private MiniGameAdapter adapter;
    private GameState state = GameState.getInstance();
    private Random random = new Random();

    private Handler updateHandler;
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigames_pro);

        tvCoins = findViewById(R.id.tvCoins);
        miniGamesRecycler = findViewById(R.id.miniGamesRecycler);

        miniGamesRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MiniGameAdapter(state.getMiniGames(), (position, miniGame) -> {
            if (miniGame.isAvailable()) {
                playMiniGame(miniGame);
            } else if (miniGame.getRemainingPlays() <= 0) {
                Toast.makeText(this, "😴 Sin partidas hoy. ¡Vuelve mañana!", Toast.LENGTH_SHORT).show();
            } else {
                long cooldown = miniGame.getCooldownRemaining();
                Toast.makeText(this, "⏱️ Espera " + formatTime(cooldown), Toast.LENGTH_SHORT).show();
            }
        });
        miniGamesRecycler.setAdapter(adapter);

        updateUI();
        startUpdateLoop();
    }

    private void playMiniGame(MiniGame miniGame) {
        // NO llamar a miniGame.startGame() aquí para la ruleta,
        // se llama en la FortuneWheelActivity

        switch (miniGame.getType()) {
            case FORTUNE_WHEEL:
                // Abrir nueva activity con ruleta animada real
                startActivity(new Intent(this, FortuneWheelActivity.class));
                break;
            case TAP_FRENZY:
                miniGame.startGame();
                state.setTotalMiniGamesPlayed(state.getTotalMiniGamesPlayed() + 1);
                state.updateMissionProgress(DailyMission.MissionType.PLAY_MINIGAME, 1);
                playTapFrenzy(miniGame);
                break;
            case LUCKY_BOX:
                miniGame.startGame();
                state.setTotalMiniGamesPlayed(state.getTotalMiniGamesPlayed() + 1);
                state.updateMissionProgress(DailyMission.MissionType.PLAY_MINIGAME, 1);
                playLuckyBox(miniGame);
                break;
            case COIN_RAIN:
                miniGame.startGame();
                state.setTotalMiniGamesPlayed(state.getTotalMiniGamesPlayed() + 1);
                state.updateMissionProgress(DailyMission.MissionType.PLAY_MINIGAME, 1);
                playCoinRain(miniGame);
                break;
            default:
                miniGame.startGame();
                playGenericGame(miniGame);
        }
    }

    private void playTapFrenzy(MiniGame miniGame) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚡ TAP FRENZY ⚡");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        TextView tvTimer = new TextView(this);
        tvTimer.setTextSize(32);
        tvTimer.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTimer.setText("10");

        TextView tvTaps = new TextView(this);
        tvTaps.setTextSize(48);
        tvTaps.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTaps.setText("0 TAPS");

        Button btnTap = new Button(this);
        btnTap.setText("👆 TAP! 👆");
        btnTap.setTextSize(24);
        btnTap.setPadding(50, 50, 50, 50);

        layout.addView(tvTimer);
        layout.addView(tvTaps);
        layout.addView(btnTap);

        builder.setView(layout);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        final int[] tapCount = {0};

        btnTap.setOnClickListener(v -> {
            tapCount[0]++;
            tvTaps.setText(tapCount[0] + " TAPS");
            btnTap.animate().scaleX(0.95f).scaleY(0.95f).setDuration(30)
                .withEndAction(() -> btnTap.animate().scaleX(1f).scaleY(1f).setDuration(30).start())
                .start();
        });

        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                double multiplier = Math.min(2.0, tapCount[0] / 50.0);
                double reward = miniGame.calculateReward(multiplier, state.getProductionPerSecond());
                state.setCoins(state.getCoins() + reward);

                String rating;
                if (tapCount[0] >= 100) rating = "🏆 LEGENDARIO";
                else if (tapCount[0] >= 70) rating = "⭐ INCREÍBLE";
                else if (tapCount[0] >= 50) rating = "👍 BIEN";
                else rating = "😅 Sigue practicando";

                new AlertDialog.Builder(MiniGamesActivity.this)
                    .setTitle("⚡ ¡Tiempo!")
                    .setMessage(rating + "\n\nTaps: " + tapCount[0] + "\nPremio: 💰 " + GameState.fmt(reward))
                    .setPositiveButton("OK", null)
                    .show();

                updateUI();
            }
        }.start();
    }

    private void playLuckyBox(MiniGame miniGame) {
        MiniGame.LuckyBox[] boxes = miniGame.generateLuckyBoxes(state.getProductionPerSecond());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📦 Lucky Box - ¡Elige una caja!");

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);

        LinearLayout boxesLayout = new LinearLayout(this);
        boxesLayout.setOrientation(LinearLayout.HORIZONTAL);
        boxesLayout.setGravity(android.view.Gravity.CENTER);

        Button[] boxButtons = new Button[3];
        final boolean[] chosen = {false};

        for (int i = 0; i < 3; i++) {
            final int index = i;
            boxButtons[i] = new Button(this);
            boxButtons[i].setText("📦\nCaja " + (i + 1));
            boxButtons[i].setTextSize(16);
            boxButtons[i].setPadding(20, 20, 20, 20);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            );
            params.setMargins(10, 0, 10, 0);
            boxButtons[i].setLayoutParams(params);

            final AlertDialog[] dialogRef = new AlertDialog[1];

            boxButtons[i].setOnClickListener(v -> {
                if (chosen[0]) return;
                chosen[0] = true;

                for (Button btn : boxButtons) btn.setEnabled(false);

                MiniGame.LuckyBox selected = boxes[index];
                state.setCoins(state.getCoins() + selected.value);

                // Revelar todas las cajas
                for (int j = 0; j < 3; j++) {
                    String reveal = j == index ? "🎁" : "📦";
                    boxButtons[j].setText(reveal + "\n" + GameState.fmt(boxes[j].value));
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (dialogRef[0] != null) dialogRef[0].dismiss();

                    new AlertDialog.Builder(MiniGamesActivity.this)
                        .setTitle(selected.message)
                        .setMessage("💰 +" + GameState.fmt(selected.value) + " coins")
                        .setPositiveButton("OK", (d, w) -> updateUI())
                        .setCancelable(false)
                        .show();
                }, 1500);
            });

            boxesLayout.addView(boxButtons[i]);
        }

        mainLayout.addView(boxesLayout);

        // Botón cancelar/volver
        Button btnCancel = new Button(this);
        btnCancel.setText("← Volver (perder intento)");
        btnCancel.setTextSize(12);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cancelParams.gravity = android.view.Gravity.CENTER;
        cancelParams.topMargin = 30;
        btnCancel.setLayoutParams(cancelParams);
        mainLayout.addView(btnCancel);

        builder.setView(mainLayout);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            updateUI();
        });

        dialog.show();
    }

    private void playCoinRain(MiniGame miniGame) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🌧️ COIN RAIN");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        TextView tvTimer = new TextView(this);
        tvTimer.setTextSize(24);
        tvTimer.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTimer.setText("⏱️ 15");

        TextView tvCoinsCollected = new TextView(this);
        tvCoinsCollected.setTextSize(36);
        tvCoinsCollected.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvCoinsCollected.setText("💰 0");

        Button btnCatch = new Button(this);
        btnCatch.setText("🪙 ATRAPAR 🪙");
        btnCatch.setTextSize(20);

        layout.addView(tvTimer);
        layout.addView(tvCoinsCollected);
        layout.addView(btnCatch);

        builder.setView(layout);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();

        final double[] coinsCollected = {0};
        final double coinValue = state.getProductionPerSecond() * 2;

        btnCatch.setOnClickListener(v -> {
            coinsCollected[0] += coinValue;
            tvCoinsCollected.setText("💰 " + GameState.fmt(coinsCollected[0]));
        });

        new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("⏱️ " + (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                state.setCoins(state.getCoins() + coinsCollected[0]);

                new AlertDialog.Builder(MiniGamesActivity.this)
                    .setTitle("🌧️ ¡Fin de la lluvia!")
                    .setMessage("Atrapaste:\n💰 " + GameState.fmt(coinsCollected[0]) + " coins")
                    .setPositiveButton("OK", null)
                    .show();

                updateUI();
            }
        }.start();
    }

    private void playGenericGame(MiniGame miniGame) {
        double reward = miniGame.calculateReward(1.0, state.getProductionPerSecond());
        state.setCoins(state.getCoins() + reward);

        new AlertDialog.Builder(this)
            .setTitle("🎮 " + miniGame.getType().name)
            .setMessage("¡Ganaste!\n\n💰 +" + GameState.fmt(reward) + " coins")
            .setPositiveButton("OK", null)
            .show();

        updateUI();
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return minutes + "m " + secs + "s";
    }

    private void updateUI() {
        tvCoins.setText("💰 " + GameState.fmt(state.getCoins()));
        adapter.notifyDataSetChanged();
    }

    private void startUpdateLoop() {
        updateHandler = new Handler(Looper.getMainLooper());
        updateRunnable = () -> {
            adapter.notifyDataSetChanged();
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
        updateUI();
        if (updateHandler != null) updateHandler.post(updateRunnable);
    }
}
