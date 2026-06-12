package com.example.juego;

import java.io.Serializable;
import java.util.Random;

/**
 * Sistema de Minijuegos para romper la monotonía
 */
public class MiniGame implements Serializable {

    public enum MiniGameType {
        FORTUNE_WHEEL("🎡", "Fortune Wheel"),
        MEMORY_MATCH("🧠", "Memory Match"),
        COIN_RAIN("🌧️", "Coin Rain"),
        BOSS_BATTLE("👾", "Boss Battle"),
        COSMIC_BILLIARDS("🎱", "Cosmic Billiards"),
        ASTEROID_DODGE("☄️", "Asteroid Dodge"),
        STAR_CATCHER("⭐", "Star Catcher"),
        GRAVITY_SLINGSHOT("🪐", "Gravity Slingshot");

        public final String emoji;
        public final String name; // nombre interno; la UI usa strings localizados

        MiniGameType(String emoji, String name) {
            this.emoji = emoji;
            this.name = name;
        }
    }

    private MiniGameType type;
    private boolean available;
    private long cooldownEndTime;
    private int timesPlayedToday;
    private int maxPlaysPerDay;
    private double baseReward;
    private Random random;

    public MiniGame(MiniGameType type, int maxPlaysPerDay, double baseReward) {
        this.type = type;
        this.maxPlaysPerDay = maxPlaysPerDay;
        this.baseReward = baseReward;
        this.available = true;
        this.cooldownEndTime = 0;
        this.timesPlayedToday = 0;
        this.random = new Random();
    }

    /**
     * Verifica si el minijuego está disponible
     */
    public boolean isAvailable() {
        if (timesPlayedToday >= maxPlaysPerDay)
            return false;
        return System.currentTimeMillis() >= cooldownEndTime;
    }

    /**
     * Tiempo restante del cooldown en segundos
     */
    public long getCooldownRemaining() {
        long remaining = cooldownEndTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    /**
     * Inicia el minijuego y establece cooldown
     */
    public void startGame() {
        timesPlayedToday++;
        // Cooldown de 30 minutos a 2 horas dependiendo del juego
        long cooldownMinutes = 30 + (type.ordinal() * 15);
        cooldownEndTime = System.currentTimeMillis() + (cooldownMinutes * 60 * 1000);
    }

    /**
     * Calcula la recompensa basada en el rendimiento
     * @param performanceMultiplier 0.0 a 2.0 basado en qué tan bien lo hizo el jugador
     */
    public double calculateReward(double performanceMultiplier, double currentProduction) {
        // La recompensa escala con la producción actual
        double scaledReward = baseReward + (currentProduction * 60); // 1 minuto de producción como base
        return scaledReward * performanceMultiplier * (1 + random.nextDouble() * 0.5); // +0-50% aleatorio
    }

    public void resetDaily() {
        timesPlayedToday = 0;
    }

    // Getters
    public MiniGameType getType() { return type; }
    public int getTimesPlayedToday() { return timesPlayedToday; }
    public int getMaxPlaysPerDay() { return maxPlaysPerDay; }
    public double getBaseReward() { return baseReward; }
    public int getRemainingPlays() { return maxPlaysPerDay - timesPlayedToday; }
    public long getCooldownEndTime() { return cooldownEndTime; }

    // Setters para persistencia
    public void setTimesPlayedToday(int times) { this.timesPlayedToday = times; }
    public void setCooldownEndTime(long time) { this.cooldownEndTime = time; }
}
