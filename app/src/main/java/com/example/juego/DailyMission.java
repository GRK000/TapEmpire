package com.example.juego;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

/**
 * Sistema de Misiones Diarias — ampliado con variedad y dificultad
 */
public class DailyMission implements Serializable {

    public enum MissionType {
        TAP_COUNT("👆", "Tap %d times"),
        EARN_COINS("💰", "Earn %s coins"),
        BUY_GENERATORS("🏭", "Buy %d generators"),
        PLAY_MINIGAME("🎮", "Play %d minigames"),
        REACH_PRODUCTION("⚙️", "Reach %s/sec production"),
        COMBO_STREAK("🔥", "Get a %dx combo"),
        CRITICAL_HITS("💥", "Get %d critical taps"),
        COLLECT_OFFLINE("😴", "Collect offline earnings"),
        BUY_UPGRADES("⬆️", "Buy %d upgrades"),
        FEED_PET("🍖", "Feed your pet %d times"),
        SPEND_COINS("💸", "Spend %s coins"),
        REACH_TAP_TOTAL("📈", "Reach %s total taps"),
        EARN_FROM_MINIGAMES("🎰", "Earn %s from minigames"),
        PET_YOUR_PET("💕", "Pet your companion %d times"),
        PLAY_ALL_MINIGAMES("🌟", "Play %d different minigames"),
        REACH_COMBO_MULTI("⚡", "Reach %sx combo multiplier");

        public final String emoji;
        public final String descriptionFormat;

        MissionType(String emoji, String descriptionFormat) {
            this.emoji = emoji;
            this.descriptionFormat = descriptionFormat;
        }
    }

    private String id;
    private MissionType type;
    private String description;
    private double targetValue;
    private double currentProgress;
    private double coinReward;
    private double bonusReward;
    private boolean completed;
    private boolean claimed;
    private int difficulty; // 1-5: Fácil → Legendaria

    public DailyMission(String id, MissionType type, double targetValue,
                        double coinReward, int difficulty) {
        this.id = id;
        this.type = type;
        this.targetValue = Math.max(1, targetValue); // Never allow 0 target
        this.coinReward = coinReward;
        this.difficulty = difficulty;
        this.currentProgress = 0;
        this.completed = false;
        this.claimed = false;

        // Generate description
        switch (type) {
            case EARN_COINS:
            case REACH_PRODUCTION:
            case SPEND_COINS:
            case EARN_FROM_MINIGAMES:
            case REACH_TAP_TOTAL:
                this.description = String.format(type.descriptionFormat, GameState.fmt(this.targetValue));
                break;
            case REACH_COMBO_MULTI:
                this.description = String.format(type.descriptionFormat, String.format("%.1f", this.targetValue));
                break;
            case COLLECT_OFFLINE:
                this.description = type.descriptionFormat; // No format args needed
                break;
            default:
                this.description = String.format(type.descriptionFormat, (int) this.targetValue);
                break;
        }
    }

    public void addProgress(double amount) {
        if (!completed) {
            currentProgress += amount;
            if (currentProgress >= targetValue) {
                currentProgress = targetValue;
                completed = true;
            }
        }
    }

    public void setProgress(double value) {
        if (!completed) {
            currentProgress = Math.min(value, targetValue);
            if (currentProgress >= targetValue) {
                completed = true;
            }
        }
    }

    public boolean claim() {
        if (completed && !claimed) {
            claimed = true;
            return true;
        }
        return false;
    }

    public double getProgressPercent() {
        if (targetValue <= 0) return 100;
        return Math.min(100, (currentProgress / targetValue) * 100);
    }

    public String getDifficultyStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(difficulty, 5); i++) sb.append("⭐");
        return sb.toString();
    }

    // Getters
    public String getId() { return id; }
    public MissionType getType() { return type; }
    public String getDescription() { return description; }
    public double getTargetValue() { return targetValue; }
    public double getCurrentProgress() { return currentProgress; }
    public double getCoinReward() { return coinReward; }
    public double getBonusReward() { return bonusReward; }
    public boolean isCompleted() { return completed; }
    public boolean isClaimed() { return claimed; }
    public int getDifficulty() { return difficulty; }

    // Setters
    public void setCurrentProgress(double progress) { this.currentProgress = progress; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
    public void setBonusReward(double bonus) { this.bonusReward = bonus; }

    /**
     * Genera misiones diarias variadas: 2 fáciles, 2 medias, 2 difíciles (6 total)
     */
    public static List<DailyMission> generateDailyMissions(double currentProduction, int prestigeLevel) {
        List<DailyMission> missions = new ArrayList<>();
        // Seed by day so missions are deterministic per day
        long daySeed = System.currentTimeMillis() / 86400000L;
        Random random = new Random(daySeed);

        // Sensible base values even at game start
        double prodSafe = Math.max(1, currentProduction);
        double baseReward = Math.max(5000, prodSafe * 300);
        int prestigeBonus = 1 + prestigeLevel;

        // === EASY MISSIONS (pick 2 of pool) ===
        List<DailyMission> easyPool = new ArrayList<>();

        easyPool.add(new DailyMission("easy_taps", MissionType.TAP_COUNT,
                200 + random.nextInt(300), baseReward, 1));

        easyPool.add(new DailyMission("easy_offline", MissionType.COLLECT_OFFLINE,
                1, baseReward * 0.8, 1));

        easyPool.add(new DailyMission("easy_feed", MissionType.FEED_PET,
                1 + random.nextInt(2), baseReward * 0.7, 1));

        easyPool.add(new DailyMission("easy_pet", MissionType.PET_YOUR_PET,
                2 + random.nextInt(3), baseReward * 0.6, 1));

        easyPool.add(new DailyMission("easy_crits", MissionType.CRITICAL_HITS,
                5 + random.nextInt(10), baseReward * 0.9, 1));

        easyPool.add(new DailyMission("easy_play1", MissionType.PLAY_MINIGAME,
                1, baseReward, 1));

        easyPool.add(new DailyMission("easy_gens", MissionType.BUY_GENERATORS,
                2 + random.nextInt(3), baseReward * 0.8, 1));

        Collections.shuffle(easyPool, random);
        missions.add(easyPool.get(0));
        missions.add(easyPool.get(1));

        // === MEDIUM MISSIONS (pick 2 of pool) ===
        List<DailyMission> medPool = new ArrayList<>();

        medPool.add(new DailyMission("med_taps", MissionType.TAP_COUNT,
                800 + random.nextInt(700), baseReward * 2.5, 2));

        medPool.add(new DailyMission("med_earn", MissionType.EARN_COINS,
                Math.max(10000, prodSafe * 600), baseReward * 3, 2));

        medPool.add(new DailyMission("med_gens", MissionType.BUY_GENERATORS,
                5 + random.nextInt(10), baseReward * 2, 2));

        medPool.add(new DailyMission("med_crits", MissionType.CRITICAL_HITS,
                20 + random.nextInt(30), baseReward * 2, 2));

        medPool.add(new DailyMission("med_combo", MissionType.COMBO_STREAK,
                10 + random.nextInt(10), baseReward * 2.5, 2));

        medPool.add(new DailyMission("med_play", MissionType.PLAY_MINIGAME,
                2 + random.nextInt(2), baseReward * 2, 2));

        medPool.add(new DailyMission("med_spend", MissionType.SPEND_COINS,
                Math.max(5000, prodSafe * 200), baseReward * 2.5, 2));

        medPool.add(new DailyMission("med_feed", MissionType.FEED_PET,
                3 + random.nextInt(3), baseReward * 2, 2));

        medPool.add(new DailyMission("med_upgrades", MissionType.BUY_UPGRADES,
                2 + random.nextInt(3), baseReward * 2.5, 2));

        Collections.shuffle(medPool, random);
        missions.add(medPool.get(0));
        missions.add(medPool.get(1));

        // === HARD MISSIONS (pick 2 of pool) ===
        List<DailyMission> hardPool = new ArrayList<>();

        hardPool.add(new DailyMission("hard_taps", MissionType.TAP_COUNT,
                1500 + random.nextInt(2000), baseReward * 6, 3));

        hardPool.add(new DailyMission("hard_combo", MissionType.COMBO_STREAK,
                25 + random.nextInt(15), baseReward * 7, 3));

        hardPool.add(new DailyMission("hard_prod", MissionType.REACH_PRODUCTION,
                Math.max(50, prodSafe + prodSafe * 0.5), baseReward * 8, 3));

        hardPool.add(new DailyMission("hard_minigames", MissionType.PLAY_ALL_MINIGAMES,
                3 + random.nextInt(3), baseReward * 6, 3));

        hardPool.add(new DailyMission("hard_earn", MissionType.EARN_COINS,
                Math.max(50000, prodSafe * 2000), baseReward * 7, 3));

        hardPool.add(new DailyMission("hard_spend", MissionType.SPEND_COINS,
                Math.max(20000, prodSafe * 800), baseReward * 6, 3));

        hardPool.add(new DailyMission("hard_crits", MissionType.CRITICAL_HITS,
                60 + random.nextInt(60), baseReward * 5, 3));

        hardPool.add(new DailyMission("hard_gens", MissionType.BUY_GENERATORS,
                15 + random.nextInt(20), baseReward * 6, 3));

        DailyMission hardComboMulti = new DailyMission("hard_multi", MissionType.REACH_COMBO_MULTI,
                3 + random.nextInt(3), baseReward * 8, 3);
        hardComboMulti.setBonusReward(0.1);
        hardPool.add(hardComboMulti);

        Collections.shuffle(hardPool, random);
        missions.add(hardPool.get(0));
        missions.add(hardPool.get(1));

        return missions;
    }
}
