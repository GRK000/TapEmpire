package com.example.juego;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de Mundos/Mapas - Cada mundo tiene su propia temática y generadores
 */
public class World implements Serializable {

    public enum WorldTheme {
        GARAGE("🏠", "Garage Startup", "Donde todo comienza", 0x7C4DFF),
        SILICON_VALLEY("🏙️", "Silicon Valley", "El corazón de la tecnología", 0x00BFA5),
        TOKYO_TECH("🗼", "Tokyo Tech", "Innovación oriental", 0xFF4081),
        DUBAI_FUTURE("🏗️", "Dubai Future", "Ciudad del mañana", 0xFFD740),
        MARS_COLONY("🔴", "Mars Colony", "Colonización espacial", 0xFF5722),
        QUANTUM_REALM("⚛️", "Quantum Realm", "Más allá de la realidad", 0x00E5FF),
        CYBER_CITY("🌃", "Cyber City", "Metrópolis neon del futuro", 0x7B1FA2),
        ATLANTIS_DEEP("🌊", "Atlantis Deep", "Civilización submarina perdida", 0x0277BD),
        VALHALLA_FORGE("🔨", "Valhalla Forge", "La forja de los dioses nórdicos", 0xBF360C),
        NEXUS_PRIME("🔮", "Nexus Prime", "El centro del multiverso", 0xAA00FF);

        public final String emoji;
        public final String name;
        public final String description;
        public final int color;

        WorldTheme(String emoji, String name, String description, int color) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.color = color;
        }
    }

    private String id;
    private WorldTheme theme;
    private boolean unlocked;
    private double unlockCost;
    private double productionMultiplier;
    private int requiredPrestigeLevel;
    private List<String> specialBonuses;
    private double totalEarningsInWorld;

    public World(String id, WorldTheme theme, double unlockCost, int requiredPrestigeLevel) {
        this.id = id;
        this.theme = theme;
        this.unlockCost = unlockCost;
        this.requiredPrestigeLevel = requiredPrestigeLevel;
        this.unlocked = unlockCost == 0;
        this.productionMultiplier = 1.0 + (theme.ordinal() * 0.25); // +25% por mundo
        this.specialBonuses = new ArrayList<>();
        this.totalEarningsInWorld = 0;

        initializeSpecialBonuses();
    }

    private void initializeSpecialBonuses() {
        switch (theme) {
            case GARAGE:
                specialBonuses.add("Tap Power +10%");
                break;
            case SILICON_VALLEY:
                specialBonuses.add("Producción +15%");
                specialBonuses.add("Costo generadores -5%");
                break;
            case TOKYO_TECH:
                specialBonuses.add("Critical Chance +5%");
                specialBonuses.add("Velocidad mejoras +20%");
                break;
            case DUBAI_FUTURE:
                specialBonuses.add("Ganancias offline +25%");
                specialBonuses.add("Bonus eventos +10%");
                break;
            case MARS_COLONY:
                specialBonuses.add("Producción x2");
                specialBonuses.add("Minijuegos +50% rewards");
                break;
            case QUANTUM_REALM:
                specialBonuses.add("TODO x2");
                specialBonuses.add("Acceso a Quantum Generators");
                break;
            case CYBER_CITY:
                specialBonuses.add("Minijuegos +30% rewards");
                specialBonuses.add("Combo duración +50%");
                break;
            case ATLANTIS_DEEP:
                specialBonuses.add("Ganancias offline +50%");
                specialBonuses.add("Cooldown minijuegos -25%");
                break;
            case VALHALLA_FORGE:
                specialBonuses.add("Tap Power x3");
                specialBonuses.add("Critical Multiplier +1.0x");
                break;
            case NEXUS_PRIME:
                specialBonuses.add("TODO x3");
                specialBonuses.add("Mascotas +100% bonus");
                specialBonuses.add("Prestigio +50% puntos");
                break;
        }
    }

    public boolean canUnlock(double coins, int prestigeLevel) {
        return coins >= unlockCost && prestigeLevel >= requiredPrestigeLevel;
    }

    public boolean tryUnlock(double coins, int prestigeLevel) {
        if (!unlocked && canUnlock(coins, prestigeLevel)) {
            unlocked = true;
            return true;
        }
        return false;
    }

    public void addEarnings(double amount) {
        totalEarningsInWorld += amount;
    }

    // Getters
    public String getId() { return id; }
    public WorldTheme getTheme() { return theme; }
    public boolean isUnlocked() { return unlocked; }
    public double getUnlockCost() { return unlockCost; }
    public double getProductionMultiplier() { return productionMultiplier; }
    public int getRequiredPrestigeLevel() { return requiredPrestigeLevel; }
    public List<String> getSpecialBonuses() { return specialBonuses; }
    public double getTotalEarningsInWorld() { return totalEarningsInWorld; }

    // Setters
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public void setTotalEarningsInWorld(double total) { this.totalEarningsInWorld = total; }
}
