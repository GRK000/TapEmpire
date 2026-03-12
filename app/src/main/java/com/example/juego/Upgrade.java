package com.example.juego;

import java.io.Serializable;

/**
 * Sistema de mejoras permanentes que afectan diferentes aspectos del juego
 */
public class Upgrade implements Serializable {

    public enum UpgradeType {
        TAP_POWER,          // Aumenta coins por tap
        TAP_MULTIPLIER,     // Multiplica coins por tap
        GLOBAL_PRODUCTION,  // Aumenta toda la producción
        GENERATOR_BOOST,    // Boost específico para un generador
        CRITICAL_CHANCE,    // Probabilidad de tap crítico
        CRITICAL_MULTIPLIER,// Multiplicador de tap crítico
        OFFLINE_BONUS       // Mejora ganancias offline
    }

    private String id;
    private String name;
    private String description;
    private String emoji;
    private UpgradeType type;
    private double baseCost;
    private double costMultiplier;
    private double effectValue;        // Valor del efecto (ej: +5 per tap)
    private int currentLevel;
    private int maxLevel;
    private String targetGeneratorId;  // Solo para GENERATOR_BOOST
    private boolean purchased;

    public Upgrade(String id, String name, String description, String emoji,
                   UpgradeType type, double baseCost, double costMultiplier,
                   double effectValue, int maxLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.type = type;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.effectValue = effectValue;
        this.currentLevel = 0;
        this.maxLevel = maxLevel;
        this.purchased = false;
    }

    /**
     * Constructor para upgrades de generador específico
     */
    public Upgrade(String id, String name, String description, String emoji,
                   double baseCost, double costMultiplier, double effectValue,
                   int maxLevel, String targetGeneratorId) {
        this(id, name, description, emoji, UpgradeType.GENERATOR_BOOST,
                baseCost, costMultiplier, effectValue, maxLevel);
        this.targetGeneratorId = targetGeneratorId;
    }

    public double getCurrentCost() {
        return baseCost * Math.pow(costMultiplier, currentLevel);
    }

    public double getTotalEffect() {
        return effectValue * currentLevel;
    }

    public boolean canUpgrade() {
        return currentLevel < maxLevel;
    }

    public boolean purchase(double availableCoins) {
        if (availableCoins >= getCurrentCost() && canUpgrade()) {
            currentLevel++;
            if (maxLevel == 1) {
                purchased = true;
            }
            return true;
        }
        return false;
    }

    public boolean isMaxLevel() {
        return currentLevel >= maxLevel;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public UpgradeType getType() { return type; }
    public double getBaseCost() { return baseCost; }
    public double getCostMultiplier() { return costMultiplier; }
    public double getEffectValue() { return effectValue; }
    public int getCurrentLevel() { return currentLevel; }
    public int getMaxLevel() { return maxLevel; }
    public String getTargetGeneratorId() { return targetGeneratorId; }
    public boolean isPurchased() { return purchased || currentLevel > 0; }

    // Setters para cargar estado
    public void setCurrentLevel(int level) { this.currentLevel = level; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }
}
