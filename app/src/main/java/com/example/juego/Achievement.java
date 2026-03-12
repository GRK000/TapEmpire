package com.example.juego;

import java.io.Serializable;

/**
 * Sistema de logros para dar objetivos al jugador
 */
public class Achievement implements Serializable {

    public enum AchievementType {
        TOTAL_TAPS,
        TOTAL_COINS_EARNED,
        COINS_OWNED,
        GENERATORS_OWNED,
        SPECIFIC_GENERATOR,
        UPGRADES_PURCHASED,
        PRESTIGE_LEVEL,
        PRODUCTION_PER_SECOND,
        // New types for workers/pets/breeding
        WORKERS_HIRED,
        WORKERS_ON_STRIKE_RESOLVED,
        PETS_OWNED,
        PETS_BRED,
        RARE_MUTATIONS,
        NURSERY_WELFARE,
        CONTRACTS_CREATED,
        HYBRID_GENERATION,
        TRAITS_DISCOVERED,
        WORLDS_UNLOCKED,
        MAX_COMBO
    }

    private String id;
    private String name;
    private String description;
    private String emoji;
    private AchievementType type;
    private double targetValue;
    private String targetId;  // Para logros específicos de generador
    private boolean unlocked;
    private double reward;    // Bonus al desbloquear
    private long unlockedTimestamp;

    public Achievement(String id, String name, String description, String emoji,
                       AchievementType type, double targetValue, double reward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.type = type;
        this.targetValue = targetValue;
        this.reward = reward;
        this.unlocked = false;
        this.unlockedTimestamp = 0;
    }

    public Achievement(String id, String name, String description, String emoji,
                       AchievementType type, double targetValue, double reward, String targetId) {
        this(id, name, description, emoji, type, targetValue, reward);
        this.targetId = targetId;
    }

    /**
     * Verifica si el logro se ha completado
     */
    public boolean checkProgress(double currentValue) {
        if (!unlocked && currentValue >= targetValue) {
            unlocked = true;
            unlockedTimestamp = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Obtiene el progreso actual como porcentaje (0-100)
     */
    public double getProgressPercent(double currentValue) {
        if (unlocked) return 100;
        return Math.min(100, (currentValue / targetValue) * 100);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public AchievementType getType() { return type; }
    public double getTargetValue() { return targetValue; }
    public String getTargetId() { return targetId; }
    public boolean isUnlocked() { return unlocked; }
    public double getReward() { return reward; }
    public long getUnlockedTimestamp() { return unlockedTimestamp; }

    // Setters para cargar estado
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public void setUnlockedTimestamp(long timestamp) { this.unlockedTimestamp = timestamp; }
}
