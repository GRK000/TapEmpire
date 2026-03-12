package com.example.juego;

/**
 * Sistema de Combos - Recompensa taps rápidos consecutivos
 */
public class ComboSystem {

    private int currentCombo;
    private int maxCombo;
    private long lastTapTime;
    private static final long COMBO_WINDOW = 500; // 500ms para mantener combo
    private static final int MAX_COMBO_MULTIPLIER = 50;

    // Thresholds para efectos especiales
    private static final int COMBO_FIRE = 5;
    private static final int COMBO_LIGHTNING = 15;
    private static final int COMBO_RAINBOW = 30;
    private static final int COMBO_LEGENDARY = 50;

    public ComboSystem() {
        this.currentCombo = 0;
        this.maxCombo = 0;
        this.lastTapTime = 0;
    }

    /**
     * Registra un tap y actualiza el combo
     * @return El multiplicador de combo actual
     */
    public double registerTap() {
        long now = System.currentTimeMillis();

        if (now - lastTapTime <= COMBO_WINDOW) {
            currentCombo++;
            if (currentCombo > maxCombo) {
                maxCombo = currentCombo;
            }
        } else {
            currentCombo = 1;
        }

        lastTapTime = now;
        return getMultiplier();
    }

    /**
     * Verifica si el combo sigue activo
     */
    public boolean isComboActive() {
        return System.currentTimeMillis() - lastTapTime <= COMBO_WINDOW && currentCombo > 1;
    }

    /**
     * Obtiene el multiplicador basado en el combo actual
     */
    public double getMultiplier() {
        if (currentCombo <= 1) return 1.0;

        // Multiplicador logarítmico para no ser demasiado OP
        // Combo 5 = x1.5, Combo 10 = x2, Combo 25 = x3, Combo 50 = x4
        double multiplier = 1.0 + (Math.log10(currentCombo) * 0.75);
        return Math.min(multiplier, 4.0); // Cap en x4
    }

    /**
     * Obtiene el efecto visual actual basado en el combo
     */
    public ComboEffect getCurrentEffect() {
        if (currentCombo >= COMBO_LEGENDARY) {
            return ComboEffect.LEGENDARY;
        } else if (currentCombo >= COMBO_RAINBOW) {
            return ComboEffect.RAINBOW;
        } else if (currentCombo >= COMBO_LIGHTNING) {
            return ComboEffect.LIGHTNING;
        } else if (currentCombo >= COMBO_FIRE) {
            return ComboEffect.FIRE;
        } else if (currentCombo > 1) {
            return ComboEffect.BASIC;
        }
        return ComboEffect.NONE;
    }

    /**
     * Obtiene el color del combo actual
     */
    public int getComboColor() {
        switch (getCurrentEffect()) {
            case LEGENDARY: return 0xFFE040FB; // Púrpura brillante
            case RAINBOW: return 0xFF00E5FF; // Cyan
            case LIGHTNING: return 0xFFFFD740; // Dorado
            case FIRE: return 0xFFFF5722; // Naranja fuego
            case BASIC: return 0xFF4CAF50; // Verde
            default: return 0xFFFFFFFF; // Blanco
        }
    }

    /**
     * Obtiene el texto del combo actual
     */
    public String getComboText() {
        if (currentCombo <= 1) return "";

        String effect = "";
        switch (getCurrentEffect()) {
            case LEGENDARY:
                effect = "🌟 LEGENDARY! 🌟";
                break;
            case RAINBOW:
                effect = "🌈 RAINBOW! 🌈";
                break;
            case LIGHTNING:
                effect = "⚡ LIGHTNING! ⚡";
                break;
            case FIRE:
                effect = "🔥 ON FIRE! 🔥";
                break;
            case BASIC:
                effect = "COMBO!";
                break;
            default:
                return "";
        }

        return effect + "\nx" + currentCombo;
    }

    /**
     * Tiempo restante del combo en milisegundos
     */
    public long getComboTimeRemaining() {
        return Math.max(0, COMBO_WINDOW - (System.currentTimeMillis() - lastTapTime));
    }

    /**
     * Porcentaje del tiempo de combo restante (0-100)
     */
    public int getComboTimePercent() {
        return (int) ((getComboTimeRemaining() / (double) COMBO_WINDOW) * 100);
    }

    public void resetCombo() {
        currentCombo = 0;
    }

    // Getters
    public int getCurrentCombo() { return currentCombo; }
    public int getMaxCombo() { return maxCombo; }

    // Setters
    public void setMaxCombo(int max) { this.maxCombo = max; }

    public enum ComboEffect {
        NONE,
        BASIC,
        FIRE,
        LIGHTNING,
        RAINBOW,
        LEGENDARY
    }
}
