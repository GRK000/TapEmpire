package com.example.juego;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private View rootLayout;
    private TextView tvScore, tvPerTap, tvPerSecond, tvPrestige, tvTapIndicator, tvCurrentWorld;
    private TextView tvComboText, tvComboMultiplier, tvEventName, tvEventTimer;
    private TextView tvPetEmoji, tvPetMood, tvPetName;
    private ProgressBar comboProgressBar;
    private LinearLayout comboContainer, eventBanner, petContainer;
    private ImageButton btnTap;
    private BottomNavigationView bottomNav;
    private ImageView tapFlash;
    private LinearLayout achievementBanner;
    private TextView tvAchievementText;

    private GameState state = GameState.getInstance();
    private Handler productionHandler;
    private Runnable productionRunnable;
    private Vibrator vibrator;
    private Random random = new Random();

    private static final String PREFS = "tap_empire_prefs";
    private static final String K_GAME_DATA = "game_data";
    private static final String K_LAST_SAVE = "last_save_time";
    private static final int PRODUCTION_UPDATE_INTERVAL = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_pro);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        bindViews();
        loadState();
        checkOfflineEarnings();
        setupListeners();
        startProductionLoop();
        render();
    }

    private void bindViews() {
        rootLayout = findViewById(android.R.id.content);
        tvScore = findViewById(R.id.tvScore);
        tvPerTap = findViewById(R.id.tvPerTap);
        tvPerSecond = findViewById(R.id.tvPerSecond);
        tvPrestige = findViewById(R.id.tvPrestige);
        tvTapIndicator = findViewById(R.id.tvTapIndicator);
        tvCurrentWorld = findViewById(R.id.tvCurrentWorld);
        btnTap = findViewById(R.id.btnTap);
        bottomNav = findViewById(R.id.bottomNav);
        tapFlash = findViewById(R.id.tapFlash);
        achievementBanner = findViewById(R.id.achievementBanner);
        tvAchievementText = findViewById(R.id.tvAchievementText);

        // Combo views
        comboContainer = findViewById(R.id.comboContainer);
        tvComboText = findViewById(R.id.tvComboText);
        tvComboMultiplier = findViewById(R.id.tvComboMultiplier);
        comboProgressBar = findViewById(R.id.comboProgressBar);

        // Event views
        eventBanner = findViewById(R.id.eventBanner);
        tvEventName = findViewById(R.id.tvEventName);
        tvEventTimer = findViewById(R.id.tvEventTimer);

        // Pet views
        petContainer = findViewById(R.id.petContainer);
        tvPetEmoji = findViewById(R.id.tvPetEmoji);
        tvPetMood = findViewById(R.id.tvPetMood);
        tvPetName = findViewById(R.id.tvPetName);
    }

    private void setupListeners() {
        btnTap.setOnClickListener(v -> performTap());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_worlds) {
                startActivity(new Intent(this, WorldsActivity.class));
                return true;
            } else if (id == R.id.nav_generators) {
                startActivity(new Intent(this, GeneratorsActivity.class));
                return true;
            } else if (id == R.id.nav_minigames) {
                startActivity(new Intent(this, MiniGamesActivity.class));
                return true;
            } else if (id == R.id.nav_more) {
                startActivity(new Intent(this, MoreActivity.class));
                return true;
            }
            return false;
        });
    }

    private void performTap() {
        GameState.TapResult result = state.tap();

        animateTap();
        showTapIndicator(result);
        updateComboUI(result);

        if (vibrator != null && vibrator.hasVibrator()) {
            int duration = result.isCritical ? 80 : 40;
            int amplitude = result.isCritical ? 200 : VibrationEffect.DEFAULT_AMPLITUDE;
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude));
        }

        render();
        checkNewAchievements();
    }

    private void updateComboUI(GameState.TapResult result) {
        ComboSystem combo = state.getComboSystem();

        if (combo.getCurrentCombo() > 1) {
            comboContainer.setVisibility(View.VISIBLE);

            String comboText = combo.getComboText();
            tvComboText.setText(comboText.split("\n")[0]);
            tvComboMultiplier.setText("x" + combo.getCurrentCombo());
            tvComboMultiplier.setTextColor(combo.getComboColor());

            comboProgressBar.setProgress(combo.getComboTimePercent());

            // Animación del combo
            comboContainer.animate()
                .scaleX(1.1f).scaleY(1.1f)
                .setDuration(50)
                .withEndAction(() -> comboContainer.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(50).start())
                .start();
        }
    }

    private void animateTap() {
        btnTap.animate()
                .scaleX(0.9f).scaleY(0.9f)
                .setDuration(50)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> btnTap.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .start())
                .start();

        tapFlash.setAlpha(0f);
        tapFlash.setScaleX(0.8f);
        tapFlash.setScaleY(0.8f);
        tapFlash.setVisibility(View.VISIBLE);
        tapFlash.animate()
                .alpha(0.6f).scaleX(1.2f).scaleY(1.2f)
                .setDuration(80)
                .withEndAction(() -> tapFlash.animate()
                        .alpha(0f).scaleX(1.4f).scaleY(1.4f)
                        .setDuration(150)
                        .withEndAction(() -> tapFlash.setVisibility(View.INVISIBLE))
                        .start())
                .start();
    }

    private void showTapIndicator(GameState.TapResult result) {
        float offsetX = (random.nextFloat() - 0.5f) * 100;
        float offsetY = random.nextFloat() * 30;

        tvTapIndicator.setTranslationX(offsetX);
        tvTapIndicator.setTranslationY(offsetY);

        String text = "+" + GameState.fmt(result.amount);
        if (result.isCritical) {
            text = "💥 " + text + " 💥";
            tvTapIndicator.setTextColor(ContextCompat.getColor(this, R.color.accent));
            tvTapIndicator.setTextSize(28);
        } else if (result.comboCount >= 10) {
            text = "🔥 " + text;
            tvTapIndicator.setTextColor(Color.parseColor("#FF5722"));
            tvTapIndicator.setTextSize(26);
        } else {
            tvTapIndicator.setTextColor(ContextCompat.getColor(this, R.color.coins_gold));
            tvTapIndicator.setTextSize(22);
        }
        tvTapIndicator.setText(text);

        tvTapIndicator.setAlpha(1f);
        tvTapIndicator.setVisibility(View.VISIBLE);

        tvTapIndicator.animate()
                .translationYBy(-120)
                .alpha(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    tvTapIndicator.setVisibility(View.INVISIBLE);
                    tvTapIndicator.setTranslationY(0);
                })
                .start();
    }

    private void startProductionLoop() {
        productionHandler = new Handler(Looper.getMainLooper());
        productionRunnable = new Runnable() {
            @Override
            public void run() {
                state.updateProduction(System.currentTimeMillis());
                render();
                updateEventUI();
                updateComboDecay();
                checkNewAchievements();
                productionHandler.postDelayed(this, PRODUCTION_UPDATE_INTERVAL);
            }
        };
        productionHandler.post(productionRunnable);
    }

    private void updateEventUI() {
        SpecialEvent event = state.getActiveEvent();
        if (event != null && event.isActive()) {
            eventBanner.setVisibility(View.VISIBLE);
            tvEventName.setText(event.getType().emoji + " " + event.getType().name + " " + event.getType().emoji);
            tvEventTimer.setText("⏱️ " + event.getRemainingTime() + "s restantes");
        } else {
            eventBanner.setVisibility(View.GONE);
        }
    }

    private void updateComboDecay() {
        ComboSystem combo = state.getComboSystem();
        if (combo.isComboActive()) {
            comboProgressBar.setProgress(combo.getComboTimePercent());
        } else if (comboContainer.getVisibility() == View.VISIBLE) {
            comboContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void stopProductionLoop() {
        if (productionHandler != null && productionRunnable != null) {
            productionHandler.removeCallbacks(productionRunnable);
        }
    }

    private void checkOfflineEarnings() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        long lastSave = sp.getLong(K_LAST_SAVE, 0);

        if (lastSave > 0) {
            double offlineEarnings = state.calculateOfflineEarnings(lastSave);

            if (offlineEarnings > 0) {
                state.addOfflineEarnings(offlineEarnings);

                new AlertDialog.Builder(this)
                        .setTitle("😴 ¡Bienvenido de vuelta!")
                        .setMessage("Mientras estuviste fuera, tu imperio generó:\n\n" +
                                "💰 " + GameState.fmt(offlineEarnings) + " coins")
                        .setPositiveButton("¡Genial!", null)
                        .show();
            }
        }
    }

    private void checkNewAchievements() {
        List<Achievement> newAchievements = state.getAndClearRecentlyUnlocked();
        for (Achievement achievement : newAchievements) {
            showAchievementUnlocked(achievement);
        }
    }

    private void showAchievementUnlocked(Achievement achievement) {
        tvAchievementText.setText(achievement.getEmoji() + " " + achievement.getName() +
                " - +" + GameState.fmt(achievement.getReward()) + " 💰");

        achievementBanner.setVisibility(View.VISIBLE);
        achievementBanner.setAlpha(0f);
        achievementBanner.setTranslationY(100);

        achievementBanner.animate()
                .alpha(1f).translationY(0)
                .setDuration(300)
                .withEndAction(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    achievementBanner.animate()
                            .alpha(0f).translationY(100)
                            .setDuration(300)
                            .withEndAction(() -> achievementBanner.setVisibility(View.GONE))
                            .start();
                }, 3000))
                .start();
    }

    private void render() {
        tvScore.setText("💰 " + GameState.fmt(state.getCoins()));
        tvPerTap.setText("👆 " + GameState.fmt(state.getPerTap()) + "/tap");
        tvPerSecond.setText("⚙️ " + GameState.fmt(state.getProductionPerSecond()) + "/seg");

        double prestigeMult = state.getPrestigeMultiplier();
        tvPrestige.setText("⭐ x" + String.format("%.2f", prestigeMult));

        World currentWorld = state.getCurrentWorld();
        tvCurrentWorld.setText(currentWorld.getTheme().emoji + " " + currentWorld.getTheme().name);

        // Cambiar fondo según el mundo
        updateWorldTheme(currentWorld);

        // Mostrar mascota activa
        updatePetDisplay();
    }

    private void updateWorldTheme(World world) {
        int backgroundRes;
        switch (world.getTheme()) {
            case SILICON_VALLEY:
                backgroundRes = R.drawable.bg_silicon_scene;
                break;
            case TOKYO_TECH:
                backgroundRes = R.drawable.bg_tokyo_scene;
                break;
            case DUBAI_FUTURE:
                backgroundRes = R.drawable.bg_dubai_scene;
                break;
            case MARS_COLONY:
                backgroundRes = R.drawable.bg_mars_scene;
                break;
            case QUANTUM_REALM:
                backgroundRes = R.drawable.bg_quantum_scene;
                break;
            case GARAGE:
            default:
                backgroundRes = R.drawable.bg_garage_scene;
                break;
        }

        View mainLayout = findViewById(R.id.rootLayout);
        if (mainLayout != null) {
            mainLayout.setBackgroundResource(backgroundRes);
        }
    }

    private void updatePetDisplay() {
        Pet activePet = state.getActivePet();
        if (activePet != null && activePet.isOwned()) {
            petContainer.setVisibility(View.VISIBLE);

            if (activePet.isAlive()) {
                tvPetEmoji.setText(activePet.getType().emoji);
                tvPetMood.setText(activePet.getStatusEmoji());
                tvPetName.setText(activePet.getCustomName());

                // Animación de la mascota (bounce suave)
                if (petContainer.getTag() == null) {
                    petContainer.setTag("animating");
                    animatePet();
                }
            } else {
                tvPetEmoji.setText(activePet.isGhost() ? "👻" : "💀");
                tvPetMood.setText("");
                tvPetName.setText("RIP");
            }
        } else {
            petContainer.setVisibility(View.GONE);
        }
    }

    private void animatePet() {
        tvPetEmoji.animate()
            .translationY(-10)
            .setDuration(500)
            .withEndAction(() ->
                tvPetEmoji.animate()
                    .translationY(0)
                    .setDuration(500)
                    .withEndAction(this::animatePet)
                    .start())
            .start();
    }

    private void loadState() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String jsonData = sp.getString(K_GAME_DATA, null);

        if (jsonData != null) {
            try {
                JSONObject json = new JSONObject(jsonData);

                state.setCoins(json.optDouble("coins", 0));
                state.setTotalCoinsEarned(json.optDouble("totalCoinsEarned", 0));
                state.setTotalTaps(json.optLong("totalTaps", 0));
                state.setPrestigeLevel(json.optInt("prestigeLevel", 0));
                state.setPrestigePoints(json.optDouble("prestigePoints", 0));
                state.setCurrentWorldIndex(json.optInt("currentWorldIndex", 0));
                state.setLastUpdateTime(System.currentTimeMillis());

                JSONArray genArray = json.optJSONArray("generators");
                if (genArray != null) {
                    List<Generator> generators = state.getGenerators();
                    for (int i = 0; i < Math.min(genArray.length(), generators.size()); i++) {
                        JSONObject genJson = genArray.getJSONObject(i);
                        Generator gen = generators.get(i);
                        gen.setOwned(genJson.optInt("owned", 0));
                        gen.setLevel(genJson.optInt("level", 1));
                        gen.setProductionMultiplier(genJson.optDouble("productionMultiplier", 1.0));
                        gen.setUnlocked(genJson.optBoolean("unlocked", false));
                    }
                }

                JSONArray upgArray = json.optJSONArray("upgrades");
                if (upgArray != null) {
                    List<Upgrade> upgrades = state.getUpgrades();
                    for (int i = 0; i < Math.min(upgArray.length(), upgrades.size()); i++) {
                        JSONObject upgJson = upgArray.getJSONObject(i);
                        Upgrade upg = upgrades.get(i);
                        upg.setCurrentLevel(upgJson.optInt("currentLevel", 0));
                    }
                }

                JSONArray achArray = json.optJSONArray("achievements");
                if (achArray != null) {
                    List<Achievement> achievements = state.getAchievements();
                    for (int i = 0; i < Math.min(achArray.length(), achievements.size()); i++) {
                        JSONObject achJson = achArray.getJSONObject(i);
                        Achievement ach = achievements.get(i);
                        ach.setUnlocked(achJson.optBoolean("unlocked", false));
                        ach.setUnlockedTimestamp(achJson.optLong("unlockedTimestamp", 0));
                    }
                }

                JSONArray worldArray = json.optJSONArray("worlds");
                if (worldArray != null) {
                    List<World> worlds = state.getWorlds();
                    for (int i = 0; i < Math.min(worldArray.length(), worlds.size()); i++) {
                        JSONObject worldJson = worldArray.getJSONObject(i);
                        World world = worlds.get(i);
                        world.setUnlocked(worldJson.optBoolean("unlocked", i == 0));
                    }
                }

                // Cargar estado de minijuegos
                JSONArray miniGamesArray = json.optJSONArray("miniGames");
                if (miniGamesArray != null) {
                    List<MiniGame> miniGames = state.getMiniGames();
                    for (int i = 0; i < Math.min(miniGamesArray.length(), miniGames.size()); i++) {
                        JSONObject gameJson = miniGamesArray.getJSONObject(i);
                        MiniGame game = miniGames.get(i);
                        game.setTimesPlayedToday(gameJson.optInt("timesPlayedToday", 0));
                        long cooldownEnd = gameJson.optLong("cooldownEndTime", 0);
                        if (cooldownEnd > System.currentTimeMillis()) {
                            game.setCooldownEndTime(cooldownEnd);
                        }
                    }
                }

                state.setMaxComboReached(json.optInt("maxComboReached", 0));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveState() {
        try {
            JSONObject json = new JSONObject();

            json.put("coins", state.getCoins());
            json.put("totalCoinsEarned", state.getTotalCoinsEarned());
            json.put("totalTaps", state.getTotalTaps());
            json.put("prestigeLevel", state.getPrestigeLevel());
            json.put("prestigePoints", state.getPrestigePoints());
            json.put("currentWorldIndex", state.getCurrentWorldIndex());

            JSONArray genArray = new JSONArray();
            for (Generator gen : state.getGenerators()) {
                JSONObject genJson = new JSONObject();
                genJson.put("owned", gen.getOwned());
                genJson.put("level", gen.getLevel());
                genJson.put("productionMultiplier", gen.getProductionMultiplier());
                genJson.put("unlocked", gen.isUnlocked());
                genArray.put(genJson);
            }
            json.put("generators", genArray);

            JSONArray upgArray = new JSONArray();
            for (Upgrade upg : state.getUpgrades()) {
                JSONObject upgJson = new JSONObject();
                upgJson.put("currentLevel", upg.getCurrentLevel());
                upgArray.put(upgJson);
            }
            json.put("upgrades", upgArray);

            JSONArray achArray = new JSONArray();
            for (Achievement ach : state.getAchievements()) {
                JSONObject achJson = new JSONObject();
                achJson.put("unlocked", ach.isUnlocked());
                achJson.put("unlockedTimestamp", ach.getUnlockedTimestamp());
                achArray.put(achJson);
            }
            json.put("achievements", achArray);

            JSONArray worldArray = new JSONArray();
            for (World world : state.getWorlds()) {
                JSONObject worldJson = new JSONObject();
                worldJson.put("unlocked", world.isUnlocked());
                worldArray.put(worldJson);
            }
            json.put("worlds", worldArray);

            // Guardar estado de minijuegos
            JSONArray miniGamesArray = new JSONArray();
            for (MiniGame game : state.getMiniGames()) {
                JSONObject gameJson = new JSONObject();
                gameJson.put("timesPlayedToday", game.getTimesPlayedToday());
                gameJson.put("cooldownEndTime", game.getCooldownEndTime());
                miniGamesArray.put(gameJson);
            }
            json.put("miniGames", miniGamesArray);

            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            sp.edit()
                    .putString(K_GAME_DATA, json.toString())
                    .putLong(K_LAST_SAVE, System.currentTimeMillis())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        startProductionLoop();
        render();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopProductionLoop();
        saveState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopProductionLoop();
    }
}
