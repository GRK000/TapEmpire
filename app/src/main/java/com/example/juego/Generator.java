package com.example.juego;

import java.io.Serializable;

/**
 * Representa un generador de ingresos pasivos (negocio)
 * Similar a los edificios en Tap to Riches
 */
public class Generator implements Serializable {
    private String id;
    private String name;
    private String description;
    private String emoji;
    private double baseProduction;     // Producción base por segundo
    private double baseCost;           // Costo base
    private double costMultiplier;     // Multiplicador de costo (típicamente 1.15)
    private int owned;                 // Cantidad que posee el jugador
    private int level;                 // Nivel de mejora del generador
    private double productionMultiplier; // Multiplicador por mejoras
    private long unlockRequirement;     // Coins necesarios para desbloquear
    private boolean unlocked;
    private int requiredWorldIndex;     // -1 = any world, 0+ = specific world

    // Upgradable business properties (accessible via Business Map)
    private int workers = 0;           // Legacy count (kept for backward compat)
    private int localSizeM2 = 10;      // Base m², expandable for +5% per 10m²
    private int locationTier = 0;      // 0=Standard, 1=Premium(+20%), 2=VIP(+50%), 3=Luxury(+100%)
    private int branches = 1;          // Number of branches (sucursales)
    private double assignedWorkerProductivity = 0.0; // Avg productivity of assigned workers (0.0-1.0+)

    public Generator(String id, String name, String description, String emoji,
                     double baseProduction, double baseCost, double costMultiplier,
                     long unlockRequirement) {
        this(id, name, description, emoji, baseProduction, baseCost, costMultiplier, unlockRequirement, -1);
    }

    public Generator(String id, String name, String description, String emoji,
                     double baseProduction, double baseCost, double costMultiplier,
                     long unlockRequirement, int requiredWorldIndex) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.baseProduction = baseProduction;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.owned = 0;
        this.level = 1;
        this.productionMultiplier = 1.0;
        this.unlockRequirement = unlockRequirement;
        this.unlocked = unlockRequirement == 0;
        this.requiredWorldIndex = requiredWorldIndex;
    }

    /**
     * Calcula el costo actual basado en la cantidad poseída
     * Fórmula: baseCost * costMultiplier^owned
     */
    public double getCurrentCost() {
        return baseCost * Math.pow(costMultiplier, owned);
    }

    /**
     * Calcula el costo de comprar N unidades
     */
    public double getCostForAmount(int amount) {
        double total = 0;
        for (int i = 0; i < amount; i++) {
            total += baseCost * Math.pow(costMultiplier, owned + i);
        }
        return total;
    }

    /**
     * Producción total por segundo de este generador
     */
    public double getProductionPerSecond() {
        return baseProduction * owned * productionMultiplier * level
                * getWorkerBonus() * getSizeBonus() * getLocationBonus()
                * getBranchBonus();
    }

    // === BUSINESS PROPERTY BONUSES ===

    /** Worker bonus: uses assigned worker productivity if available, else legacy count */
    public double getWorkerBonus() {
        if (assignedWorkerProductivity > 0) {
            return 1.0 + assignedWorkerProductivity;
        }
        return 1.0 + (workers * 0.08);
    }

    /** Each extra 10m² adds +5% production */
    public double getSizeBonus() {
        return 1.0 + (Math.max(0, localSizeM2 - 10) / 10.0 * 0.05);
    }

    /** Location tier bonus: Standard=1x, Premium=1.2x, VIP=1.5x, Luxury=2x */
    public double getLocationBonus() {
        switch (locationTier) {
            case 1: return 1.2;
            case 2: return 1.5;
            case 3: return 2.0;
            default: return 1.0;
        }
    }

    /** Each branch adds +50% production */
    public double getBranchBonus() {
        return 1.0 + (Math.max(0, branches - 1) * 0.5);
    }

    /** Max workers = (m² / 5) * branches */
    public int getMaxWorkerCapacity() {
        return (localSizeM2 / 5) * branches;
    }

    /** Cost of opening a new branch */
    public double getBranchCost() {
        return baseCost * 50 * Math.pow(4, branches - 1);
    }

    /** Max 5 branches */
    public boolean canOpenBranch() {
        return branches < 5;
    }

    public double getWorkerUpgradeCost() {
        return baseCost * 5 * Math.pow(1.8, workers);
    }

    public double getSizeUpgradeCost() {
        int expansions = Math.max(0, (localSizeM2 - 10) / 10);
        return baseCost * 3 * Math.pow(2.0, expansions);
    }

    public double getLocationUpgradeCost() {
        return baseCost * 20 * Math.pow(5, locationTier);
    }

    public boolean canUpgradeLocation() {
        return locationTier < 3;
    }

    /**
     * Compra una unidad del generador
     */
    public boolean buy(double availableCoins) {
        double cost = getCurrentCost();
        if (availableCoins >= cost && unlocked) {
            owned++;
            return true;
        }
        return false;
    }

    /**
     * Mejora el nivel del generador (multiplica producción)
     */
    public void upgrade() {
        level++;
        productionMultiplier *= 1.5; // +50% por nivel
    }

    /**
     * Calcula el costo de la siguiente mejora de nivel
     */
    public double getUpgradeCost() {
        return baseCost * 10 * Math.pow(2, level - 1);
    }

    /**
     * Intenta desbloquear el generador
     */
    public boolean tryUnlock(double totalCoinsEarned) {
        if (!unlocked && totalCoinsEarned >= unlockRequirement) {
            unlocked = true;
            return true;
        }
        return false;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public double getBaseProduction() { return baseProduction; }
    public double getBaseCost() { return baseCost; }
    public double getCostMultiplier() { return costMultiplier; }
    public int getOwned() { return owned; }
    public int getLevel() { return level; }
    public double getProductionMultiplier() { return productionMultiplier; }
    public long getUnlockRequirement() { return unlockRequirement; }
    public boolean isUnlocked() { return unlocked; }

    public void setOwned(int owned) { this.owned = owned; }
    public void setLevel(int level) { this.level = level; }
    public void setProductionMultiplier(double productionMultiplier) {
        this.productionMultiplier = productionMultiplier;
    }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public int getRequiredWorldIndex() { return requiredWorldIndex; }
    public int getWorkers() { return workers; }
    public void setWorkers(int workers) { this.workers = workers; }
    public int getLocalSizeM2() { return localSizeM2; }
    public void setLocalSizeM2(int size) { this.localSizeM2 = size; }
    public int getLocationTier() { return locationTier; }
    public void setLocationTier(int tier) { this.locationTier = tier; }
    public int getBranches() { return branches; }
    public void setBranches(int branches) { this.branches = branches; }
    public double getAssignedWorkerProductivity() { return assignedWorkerProductivity; }
    public void setAssignedWorkerProductivity(double p) { this.assignedWorkerProductivity = p; }

    public String getLocationTierName() {
        switch (locationTier) {
            case 1: return "Premium";
            case 2: return "VIP";
            case 3: return "Luxury";
            default: return "Standard";
        }
    }
}
