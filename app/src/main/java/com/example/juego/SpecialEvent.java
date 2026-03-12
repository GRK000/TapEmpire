package com.example.juego;

import java.io.Serializable;
import java.util.Random;

/**
 * Sistema de Eventos Temporales que aparecen aleatoriamente
 */
public class SpecialEvent implements Serializable {

    public enum EventType {
        GOLDEN_HOUR("🌟", "Golden Hour", "¡x3 producción durante 60 segundos!",
                60000, 3.0, 0),
        CRITICAL_STORM("⚡", "Critical Storm", "¡100% probabilidad de crítico por 30 segundos!",
                30000, 1.0, 1.0),
        COIN_SHOWER("💰", "Coin Shower", "¡Lluvia de monedas! Toca para recogerlas",
                20000, 1.0, 0),
        MYSTERY_MERCHANT("🎭", "Mystery Merchant", "¡Ofertas especiales por tiempo limitado!",
                45000, 1.0, 0),
        DOUBLE_TAP("👆", "Double Tap", "¡Cada tap cuenta doble!",
                40000, 1.0, 0),
        SPEED_BOOST("🚀", "Speed Boost", "¡Generadores producen x5 más rápido!",
                30000, 5.0, 0),
        LUCKY_STREAK("🍀", "Lucky Streak", "¡Probabilidad de premio x10!",
                25000, 1.0, 0),
        BOSS_RAID("👾", "Boss Raid", "¡Derrota al boss para premios épicos!",
                60000, 1.0, 0);

        public final String emoji;
        public final String name;
        public final String description;
        public final long duration; // milisegundos
        public final double productionMultiplier;
        public final double criticalBonus;

        EventType(String emoji, String name, String description,
                  long duration, double prodMult, double critBonus) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.duration = duration;
            this.productionMultiplier = prodMult;
            this.criticalBonus = critBonus;
        }
    }

    private EventType type;
    private long startTime;
    private long endTime;
    private boolean active;
    private boolean completed;
    private double bonusEarned;
    private int tapsInEvent;
    private int bossHealth;
    private int bossMaxHealth;

    public SpecialEvent(EventType type) {
        this.type = type;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + type.duration;
        this.active = true;
        this.completed = false;
        this.bonusEarned = 0;
        this.tapsInEvent = 0;

        if (type == EventType.BOSS_RAID) {
            this.bossMaxHealth = 100;
            this.bossHealth = bossMaxHealth;
        }
    }

    /**
     * Verifica si el evento sigue activo
     */
    public boolean isActive() {
        if (!active) return false;
        if (System.currentTimeMillis() > endTime) {
            active = false;
            completed = true;
        }
        return active;
    }

    /**
     * Tiempo restante en segundos
     */
    public long getRemainingTime() {
        return Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
    }

    /**
     * Registra un tap durante el evento
     */
    public void registerTap(double tapValue) {
        tapsInEvent++;
        bonusEarned += tapValue * (type.productionMultiplier - 1);

        if (type == EventType.BOSS_RAID) {
            bossHealth -= 1 + (tapsInEvent / 10); // Daño escala con taps
        }
    }

    /**
     * Verifica si el boss fue derrotado (para BOSS_RAID)
     */
    public boolean isBossDefeated() {
        return type == EventType.BOSS_RAID && bossHealth <= 0;
    }

    /**
     * Calcula recompensa del evento
     */
    public double calculateReward(double currentProduction) {
        double baseReward = currentProduction * (type.duration / 1000);

        switch (type) {
            case GOLDEN_HOUR:
            case SPEED_BOOST:
                return bonusEarned;
            case COIN_SHOWER:
                return baseReward * tapsInEvent * 0.1;
            case BOSS_RAID:
                if (isBossDefeated()) {
                    return baseReward * 10; // Gran recompensa por derrotar boss
                }
                return baseReward * (1 - (bossHealth / (double) bossMaxHealth));
            case DOUBLE_TAP:
                return bonusEarned;
            default:
                return baseReward;
        }
    }

    /**
     * Genera un evento aleatorio
     */
    public static SpecialEvent generateRandomEvent() {
        Random random = new Random();
        EventType[] types = EventType.values();

        // Algunos eventos son más raros que otros
        int roll = random.nextInt(100);
        EventType selectedType;

        if (roll < 25) {
            selectedType = EventType.GOLDEN_HOUR;
        } else if (roll < 45) {
            selectedType = EventType.DOUBLE_TAP;
        } else if (roll < 60) {
            selectedType = EventType.COIN_SHOWER;
        } else if (roll < 75) {
            selectedType = EventType.CRITICAL_STORM;
        } else if (roll < 85) {
            selectedType = EventType.SPEED_BOOST;
        } else if (roll < 92) {
            selectedType = EventType.LUCKY_STREAK;
        } else if (roll < 97) {
            selectedType = EventType.MYSTERY_MERCHANT;
        } else {
            selectedType = EventType.BOSS_RAID; // 3% de probabilidad
        }

        return new SpecialEvent(selectedType);
    }

    // Getters
    public EventType getType() { return type; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public boolean isCompleted() { return completed; }
    public double getBonusEarned() { return bonusEarned; }
    public int getTapsInEvent() { return tapsInEvent; }
    public int getBossHealth() { return bossHealth; }
    public int getBossMaxHealth() { return bossMaxHealth; }

    public double getBossHealthPercent() {
        if (bossMaxHealth == 0) return 0;
        return (bossHealth / (double) bossMaxHealth) * 100;
    }
}
