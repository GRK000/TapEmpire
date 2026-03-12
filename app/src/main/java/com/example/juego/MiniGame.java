package com.example.juego;

import java.io.Serializable;
import java.util.Random;

/**
 * Sistema de Minijuegos para romper la monotonía
 */
public class MiniGame implements Serializable {

    public enum MiniGameType {
        FORTUNE_WHEEL("🎡", "Ruleta de la Fortuna", "Gira y gana premios increíbles"),
        TAP_FRENZY("⚡", "Tap Frenzy", "¡Tapea lo más rápido posible en 10 segundos!"),
        LUCKY_BOX("📦", "Lucky Box", "Elige una caja, ¿cuál tendrá el tesoro?"),
        MEMORY_MATCH("🧠", "Memory Match", "Encuentra los pares de íconos tech"),
        COIN_RAIN("🌧️", "Coin Rain", "¡Atrapa todas las monedas que puedas!"),
        BOSS_BATTLE("👾", "Boss Battle", "Derrota al bug gigante con taps!"),
        COSMIC_BILLIARDS("🎱", "Cosmic Billiards", "Lanza orbes cósmicos contra estrellas"),
        ASTEROID_DODGE("☄️", "Asteroid Dodge", "Esquiva asteroides y recoge cristales"),
        STAR_CATCHER("⭐", "Star Catcher", "Atrapa estrellas fugaces antes de que desaparezcan"),
        GRAVITY_SLINGSHOT("🪐", "Gravity Slingshot", "Usa la gravedad planetaria para alcanzar el objetivo");

        public final String emoji;
        public final String name;
        public final String description;

        MiniGameType(String emoji, String name, String description) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
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

    /**
     * Genera premios para la ruleta de la fortuna
     */
    public FortuneWheelPrize[] generateWheelPrizes(double currentProduction) {
        FortuneWheelPrize[] prizes = new FortuneWheelPrize[8];
        double basePrize = currentProduction * 30; // 30 segundos de producción

        prizes[0] = new FortuneWheelPrize("💰 x1", basePrize, 25);
        prizes[1] = new FortuneWheelPrize("💰 x2", basePrize * 2, 20);
        prizes[2] = new FortuneWheelPrize("💰 x5", basePrize * 5, 15);
        prizes[3] = new FortuneWheelPrize("💰 x10", basePrize * 10, 10);
        prizes[4] = new FortuneWheelPrize("⚡ 2x Prod 1min", 0, 12); // Bonus especial
        prizes[5] = new FortuneWheelPrize("🎯 +10% Crit", 0, 8); // Bonus especial
        prizes[6] = new FortuneWheelPrize("💎 JACKPOT", basePrize * 50, 5);
        prizes[7] = new FortuneWheelPrize("😢 Nada", 0, 5);

        return prizes;
    }

    /**
     * Genera cajas para Lucky Box
     */
    public LuckyBox[] generateLuckyBoxes(double currentProduction) {
        LuckyBox[] boxes = new LuckyBox[3];
        double basePrize = currentProduction * 60;

        // Mezclar aleatoriamente las recompensas
        int jackpotIndex = random.nextInt(3);
        int emptyIndex = (jackpotIndex + 1 + random.nextInt(2)) % 3;
        int normalIndex = 3 - jackpotIndex - emptyIndex;

        boxes[jackpotIndex] = new LuckyBox("📦", basePrize * 10, "¡JACKPOT! 💎");
        boxes[normalIndex] = new LuckyBox("📦", basePrize * 2, "¡Buen premio! 💰");
        boxes[emptyIndex] = new LuckyBox("📦", basePrize * 0.5, "Premio de consolación 🎁");

        return boxes;
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

    // Clases internas para resultados
    public static class FortuneWheelPrize {
        public String name;
        public double value;
        public int probability; // porcentaje

        public FortuneWheelPrize(String name, double value, int probability) {
            this.name = name;
            this.value = value;
            this.probability = probability;
        }
    }

    public static class LuckyBox {
        public String emoji;
        public double value;
        public String message;
        public boolean opened = false;

        public LuckyBox(String emoji, double value, String message) {
            this.emoji = emoji;
            this.value = value;
            this.message = message;
        }
    }
}
