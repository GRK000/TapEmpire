package com.example.juego;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema de Eventos — Afectan producción, trabajadores, mascotas, contratos y estabilidad.
 * Cada evento tiene categoría, duración, efectos y opciones de decisión del jugador.
 */
public class GameEvent implements Serializable {

    public enum EventCategory { GLOBAL, WORLD_LOCAL, CORPORATE }

    public enum EventType {
        // === GLOBAL (13) ===
        GLOBAL_ECONOMIC_CRISIS(EventCategory.GLOBAL, "📉", 600_000, true, false, false),
        GLOBAL_ECONOMIC_BOOM(EventCategory.GLOBAL, "📈", 480_000, true, false, false),
        LEGAL_REFORM(EventCategory.GLOBAL, "⚖️", 720_000, true, false, false),
        PANDEMIC_OUTBREAK(EventCategory.GLOBAL, "🦠", 720_000, true, false, false),
        GOLDEN_AGE(EventCategory.GLOBAL, "✨", 300_000, false, false, false),
        ALIEN_CONTACT(EventCategory.GLOBAL, "👽", 600_000, true, false, false),
        GALACTIC_WAR(EventCategory.GLOBAL, "⚔️", 900_000, true, false, false),
        TRADE_AGREEMENT(EventCategory.GLOBAL, "🤝", 480_000, true, false, false),
        CLIMATE_DISASTER(EventCategory.GLOBAL, "🌪️", 600_000, true, false, false),
        MARKET_CRASH(EventCategory.GLOBAL, "💥", 540_000, true, false, false),
        GALACTIC_FESTIVAL(EventCategory.GLOBAL, "🎪", 360_000, true, false, false),
        DIPLOMATIC_INCIDENT(EventCategory.GLOBAL, "🏛️", 600_000, true, false, false),
        DARK_MATTER_SURGE(EventCategory.GLOBAL, "🌑", 420_000, false, false, false),

        // === WORLD_LOCAL (10) ===
        SCIENTIFIC_DISCOVERY(EventCategory.WORLD_LOCAL, "🔬", 480_000, true, false, false),
        TECH_BREAKTHROUGH(EventCategory.WORLD_LOCAL, "💡", 420_000, true, false, false),
        RESOURCE_SHORTAGE(EventCategory.WORLD_LOCAL, "⛽", 480_000, true, false, false),
        ENERGY_BLACKOUT(EventCategory.WORLD_LOCAL, "🔌", 360_000, true, false, false),
        TERRAFORMING_SUCCESS(EventCategory.WORLD_LOCAL, "🌱", 480_000, true, false, false),
        SPACE_DEBRIS(EventCategory.WORLD_LOCAL, "☄️", 420_000, true, false, false),
        CYBER_ATTACK(EventCategory.WORLD_LOCAL, "🖥️", 540_000, true, false, false),
        INNOVATION_GRANT(EventCategory.WORLD_LOCAL, "🎓", 360_000, true, false, false),
        SUPPLY_CHAIN_CRISIS(EventCategory.WORLD_LOCAL, "📦", 480_000, true, false, false),
        MUTANT_OUTBREAK(EventCategory.WORLD_LOCAL, "🧬", 600_000, true, false, true),

        // === CORPORATE (12) ===
        SECTOR_STRIKE(EventCategory.CORPORATE, "✊", 360_000, true, true, false),
        COMPETITOR_INVASION(EventCategory.CORPORATE, "🏴\u200D☠️", 600_000, true, false, false),
        UNION_MOVEMENT(EventCategory.CORPORATE, "🪧", 480_000, true, true, false),
        ANIMAL_WELFARE_SCANDAL(EventCategory.CORPORATE, "🐾", 600_000, true, false, true),
        COMPANY_OF_YEAR(EventCategory.CORPORATE, "🏆", 300_000, true, false, false),
        CORRUPTION_SCANDAL(EventCategory.CORPORATE, "🕵️", 600_000, true, false, false),
        TAX_AUDIT(EventCategory.CORPORATE, "📊", 480_000, true, false, false),
        WORKER_TRAINING(EventCategory.CORPORATE, "📚", 420_000, true, true, false),
        ESPIONAGE(EventCategory.CORPORATE, "🔍", 540_000, true, false, false),
        LABOR_SHORTAGE(EventCategory.CORPORATE, "👷", 480_000, true, true, false),
        PET_COMPETITION(EventCategory.CORPORATE, "🏅", 360_000, true, false, true),
        INVESTOR_INTEREST(EventCategory.CORPORATE, "💼", 420_000, true, false, false);

        public final EventCategory category;
        public final String emoji;
        public final long defaultDurationMs;
        public final boolean hasChoices;
        public final boolean requiresWorkers;
        public final boolean requiresPets;

        EventType(EventCategory cat, String emoji, long dur, boolean choices,
                  boolean requiresWorkers, boolean requiresPets) {
            this.category = cat;
            this.emoji = emoji;
            this.defaultDurationMs = dur;
            this.hasChoices = choices;
            this.requiresWorkers = requiresWorkers;
            this.requiresPets = requiresPets;
        }

        public boolean isMajor() { return category == EventCategory.GLOBAL; }
    }

    // === Inner class: Player choice ===
    public static class EventChoice implements Serializable {
        public final String labelKey;   // string resource key
        public final String emoji;
        public final Map<String, Double> effects; // e.g. "production_mult" → 0.2, "stability" → 5

        public EventChoice(String labelKey, String emoji, Map<String, Double> effects) {
            this.labelKey = labelKey;
            this.emoji = emoji;
            this.effects = effects != null ? effects : new HashMap<>();
        }
    }

    // === Effect keys (constants) ===
    public static final String EFF_PRODUCTION_MULT = "production_mult";      // +/- multiplier (e.g. 0.25 = +25%)
    public static final String EFF_STRIKE_CHANCE = "strike_chance";          // +/- modifier
    public static final String EFF_CONTRACT_COST = "contract_cost_mult";     // multiplier
    public static final String EFF_MUTATION_CHANCE = "mutation_chance";       // +/- modifier
    public static final String EFF_WELFARE = "welfare";                       // direct +/- change
    public static final String EFF_STABILITY = "stability";                   // direct +/- change
    public static final String EFF_MOOD_ALL = "mood_all";                     // +1 or -1 tick for all
    public static final String EFF_COINS_PERCENT = "coins_percent";           // +/- % of current coins
    public static final String EFF_COINS_FLAT = "coins_flat";                 // flat coins gain/loss

    // === Fields ===
    private String id;
    private EventType type;
    private long durationMs;
    private long startTime;
    private boolean active;
    private boolean resolved;
    private boolean autoResolved;     // true if resolved automatically (player didn't choose)
    private int choiceIndex;          // -1 = not chosen, 0+ = choice made
    private List<EventChoice> choices;
    private Map<String, Double> baseEffects;  // effects that apply while active (before choice)

    public GameEvent(EventType type) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.durationMs = type.defaultDurationMs;
        this.startTime = System.currentTimeMillis();
        this.active = true;
        this.resolved = false;
        this.autoResolved = false;
        this.choiceIndex = -1;
        this.choices = new ArrayList<>();
        this.baseEffects = new HashMap<>();
    }

    // === State queries ===
    public boolean isActive() {
        if (!active) return false;
        if (System.currentTimeMillis() - startTime > durationMs) {
            active = false;
            return false;
        }
        return true;
    }

    public long getRemainingMs() {
        long remaining = durationMs - (System.currentTimeMillis() - startTime);
        return Math.max(0, remaining);
    }

    public boolean needsChoice() {
        return type.hasChoices && choiceIndex < 0 && isActive();
    }

    /** Resolve the event with the player's choice */
    public Map<String, Double> resolve(int choice) {
        if (choice < 0 || choice >= choices.size()) return new HashMap<>();
        this.choiceIndex = choice;
        this.resolved = true;
        this.autoResolved = false;
        return choices.get(choice).effects;
    }

    /**
     * Find the worst choice index by computing a "damage score" for each option.
     * Lower (more negative) score = worse for the player.
     */
    public int findWorstChoiceIndex() {
        if (choices.isEmpty()) return -1;
        int worstIdx = 0;
        double worstScore = Double.MAX_VALUE;
        for (int i = 0; i < choices.size(); i++) {
            double score = 0;
            for (Map.Entry<String, Double> eff : choices.get(i).effects.entrySet()) {
                String key = eff.getKey();
                double val_ = eff.getValue();
                // Weight each effect type by impact severity
                switch (key) {
                    case EFF_COINS_PERCENT:    score += val_ * 5.0; break;  // losing coins hurts most
                    case EFF_PRODUCTION_MULT:  score += val_ * 4.0; break;
                    case EFF_STABILITY:        score += val_ * 3.0; break;
                    case EFF_WELFARE:          score += val_ * 2.0; break;
                    case EFF_MOOD_ALL:         score += val_ * 2.5; break;
                    case EFF_STRIKE_CHANCE:    score -= val_ * 3.0; break;  // higher strike = worse
                    case EFF_CONTRACT_COST:    score -= val_ * 2.0; break;  // higher cost = worse
                    case EFF_COINS_FLAT:       score += val_ * 1.0; break;
                    case EFF_MUTATION_CHANCE:  score += val_ * 0.5; break;
                    default:                  score += val_; break;
                }
            }
            if (score < worstScore) {
                worstScore = score;
                worstIdx = i;
            }
        }
        return worstIdx;
    }

    /**
     * Auto-resolve with the worst possible choice (penalty for not deciding).
     * Returns the effects of the worst choice.
     */
    public Map<String, Double> autoResolveWithWorst() {
        int worstIdx = findWorstChoiceIndex();
        if (worstIdx < 0) return new HashMap<>();
        this.choiceIndex = worstIdx;
        this.resolved = true;
        this.autoResolved = true;
        this.active = false;
        return choices.get(worstIdx).effects;
    }

    /** Get current active effects (base + chosen) */
    public Map<String, Double> getCurrentEffects() {
        Map<String, Double> result = new HashMap<>(baseEffects);
        if (choiceIndex >= 0 && choiceIndex < choices.size()) {
            for (Map.Entry<String, Double> e : choices.get(choiceIndex).effects.entrySet()) {
                result.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }
        return result;
    }

    public double getEffect(String key) {
        return getCurrentEffects().getOrDefault(key, 0.0);
    }

    // === Getters/Setters ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public EventType getType() { return type; }
    public void setType(EventType t) { this.type = t; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long d) { this.durationMs = d; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long t) { this.startTime = t; }
    public boolean getActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean r) { this.resolved = r; }
    public boolean isAutoResolved() { return autoResolved; }
    public void setAutoResolved(boolean a) { this.autoResolved = a; }
    public int getChoiceIndex() { return choiceIndex; }
    public void setChoiceIndex(int c) { this.choiceIndex = c; }
    public List<EventChoice> getChoices() { return choices; }
    public void setChoices(List<EventChoice> c) { this.choices = c; }
    public Map<String, Double> getBaseEffects() { return baseEffects; }
    public void setBaseEffects(Map<String, Double> e) { this.baseEffects = e; }

    // ========================================
    // FACTORY: Create fully-configured events
    // ========================================

    public static GameEvent create(EventType type) {
        GameEvent e = new GameEvent(type);
        switch (type) {
            case GLOBAL_ECONOMIC_CRISIS:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.20);
                e.baseEffects.put(EFF_STRIKE_CHANCE, 0.15);
                e.choices.add(new EventChoice("event_crisis_cut", "✂️", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_MOOD_ALL, -1.0, EFF_STABILITY, -5.0)));
                e.choices.add(new EventChoice("event_crisis_loan", "🏦", mapOf(
                        EFF_COINS_PERCENT, -0.30, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_crisis_ignore", "🤷", mapOf(
                        EFF_PRODUCTION_MULT, -0.20, EFF_STABILITY, -8.0)));
                break;

            case GLOBAL_ECONOMIC_BOOM:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.25);
                e.baseEffects.put(EFF_STRIKE_CHANCE, -0.10);
                e.choices.add(new EventChoice("event_boom_invest", "💎", mapOf(
                        EFF_PRODUCTION_MULT, 0.25, EFF_COINS_PERCENT, -0.15)));
                e.choices.add(new EventChoice("event_boom_save", "🏦", mapOf(
                        EFF_STABILITY, 3.0)));
                break;

            case LEGAL_REFORM:
                e.baseEffects.put(EFF_CONTRACT_COST, 0.15);
                e.choices.add(new EventChoice("event_legal_comply", "✅", mapOf(
                        EFF_STABILITY, 5.0, EFF_CONTRACT_COST, 0.10)));
                e.choices.add(new EventChoice("event_legal_lobby", "💼", mapOf(
                        EFF_STABILITY, -5.0, EFF_CONTRACT_COST, -0.15, EFF_COINS_PERCENT, -0.05)));
                break;

            case PANDEMIC_OUTBREAK:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.15);
                e.baseEffects.put(EFF_WELFARE, -10.0);
                e.choices.add(new EventChoice("event_pandemic_quarantine", "🔒", mapOf(
                        EFF_PRODUCTION_MULT, -0.35, EFF_STABILITY, 8.0, EFF_WELFARE, 5.0)));
                e.choices.add(new EventChoice("event_pandemic_ignore", "🤷", mapOf(
                        EFF_WELFARE, -15.0, EFF_MOOD_ALL, -1.0, EFF_STABILITY, -10.0)));
                break;

            case GOLDEN_AGE:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 1.0);
                e.baseEffects.put(EFF_MOOD_ALL, 1.0);
                e.baseEffects.put(EFF_STABILITY, 5.0);
                // No choices needed — pure reward
                break;

            case ALIEN_CONTACT:
                e.choices.add(new EventChoice("event_alien_diplomacy", "🤝", mapOf(
                        EFF_PRODUCTION_MULT, 0.30, EFF_STABILITY, 8.0)));
                e.choices.add(new EventChoice("event_alien_exploit", "⛏️", mapOf(
                        EFF_COINS_PERCENT, 0.25, EFF_STABILITY, -10.0, EFF_MUTATION_CHANCE, 0.15)));
                e.choices.add(new EventChoice("event_alien_ignore", "🚫", mapOf(
                        EFF_STABILITY, -2.0)));
                break;

            case SCIENTIFIC_DISCOVERY:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.10);
                e.choices.add(new EventChoice("event_science_patent", "📜", mapOf(
                        EFF_PRODUCTION_MULT, 0.20)));
                e.choices.add(new EventChoice("event_science_open", "🌐", mapOf(
                        EFF_MUTATION_CHANCE, 0.10, EFF_STABILITY, 5.0)));
                break;

            case TECH_BREAKTHROUGH:
                e.choices.add(new EventChoice("event_tech_implement", "🚀", mapOf(
                        EFF_PRODUCTION_MULT, 0.40, EFF_COINS_PERCENT, -0.10)));
                e.choices.add(new EventChoice("event_tech_sell", "💰", mapOf(
                        EFF_COINS_PERCENT, 0.20)));
                break;

            case RESOURCE_SHORTAGE:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.15);
                e.choices.add(new EventChoice("event_resource_import", "📦", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_PRODUCTION_MULT, 0.15)));
                e.choices.add(new EventChoice("event_resource_ration", "📋", mapOf(
                        EFF_PRODUCTION_MULT, -0.10, EFF_STABILITY, 3.0)));
                break;

            case SECTOR_STRIKE:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.30);
                e.choices.add(new EventChoice("event_strike_negotiate", "🤝", mapOf(
                        EFF_COINS_PERCENT, -0.08, EFF_MOOD_ALL, 1.0, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_strike_replace", "👥", mapOf(
                        EFF_STABILITY, -8.0, EFF_MOOD_ALL, -1.0)));
                break;

            case COMPETITOR_INVASION:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.10);
                e.choices.add(new EventChoice("event_competitor_war", "⚔️", mapOf(
                        EFF_PRODUCTION_MULT, -0.10, EFF_COINS_PERCENT, -0.15, EFF_STABILITY, 3.0)));
                e.choices.add(new EventChoice("event_competitor_ally", "🤝", mapOf(
                        EFF_PRODUCTION_MULT, 0.10, EFF_STABILITY, -5.0)));
                break;

            case UNION_MOVEMENT:
                e.baseEffects.put(EFF_STRIKE_CHANCE, 0.20);
                e.choices.add(new EventChoice("event_union_concede", "✅", mapOf(
                        EFF_MOOD_ALL, 1.0, EFF_STABILITY, 5.0, EFF_COINS_PERCENT, -0.05)));
                e.choices.add(new EventChoice("event_union_suppress", "🚫", mapOf(
                        EFF_MOOD_ALL, -1.0, EFF_STABILITY, -8.0, EFF_PRODUCTION_MULT, 0.10)));
                break;

            case ANIMAL_WELFARE_SCANDAL:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.10);
                e.baseEffects.put(EFF_WELFARE, -15.0);
                e.choices.add(new EventChoice("event_welfare_reform", "💚", mapOf(
                        EFF_WELFARE, 25.0, EFF_COINS_PERCENT, -0.12, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_welfare_pr", "📢", mapOf(
                        EFF_STABILITY, -10.0)));
                break;

            case COMPANY_OF_YEAR:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.25);
                e.baseEffects.put(EFF_MOOD_ALL, 1.0);
                e.choices.add(new EventChoice("event_award_celebrate", "🎉", mapOf(
                        EFF_PRODUCTION_MULT, 0.10, EFF_STABILITY, 3.0)));
                e.choices.add(new EventChoice("event_award_humble", "🙏", mapOf(
                        EFF_STABILITY, 10.0)));
                break;

            case CORRUPTION_SCANDAL:
                e.baseEffects.put(EFF_STABILITY, -5.0);
                e.choices.add(new EventChoice("event_corrupt_investigate", "🔍", mapOf(
                        EFF_STABILITY, 10.0, EFF_COINS_PERCENT, -0.15)));
                e.choices.add(new EventChoice("event_corrupt_cover", "🤫", mapOf(
                        EFF_STABILITY, -10.0)));
                break;

            // ====== NEW GLOBAL EVENTS ======

            case GALACTIC_WAR:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.25);
                e.baseEffects.put(EFF_STABILITY, -10.0);
                e.choices.add(new EventChoice("event_war_arms", "🔫", mapOf(
                        EFF_COINS_PERCENT, 0.20, EFF_STABILITY, -15.0, EFF_MOOD_ALL, -1.0)));
                e.choices.add(new EventChoice("event_war_neutral", "🏳️", mapOf(
                        EFF_PRODUCTION_MULT, -0.10, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_war_diplomacy", "🕊️", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_STABILITY, 12.0, EFF_MOOD_ALL, 1.0)));
                break;

            case TRADE_AGREEMENT:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.15);
                e.choices.add(new EventChoice("event_trade_accept", "✅", mapOf(
                        EFF_PRODUCTION_MULT, 0.20, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_trade_renegotiate", "📝", mapOf(
                        EFF_COINS_PERCENT, 0.10, EFF_STABILITY, -3.0)));
                break;

            case CLIMATE_DISASTER:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.20);
                e.baseEffects.put(EFF_WELFARE, -10.0);
                e.choices.add(new EventChoice("event_climate_rebuild", "🔨", mapOf(
                        EFF_COINS_PERCENT, -0.20, EFF_STABILITY, 8.0, EFF_WELFARE, 10.0)));
                e.choices.add(new EventChoice("event_climate_relocate", "🚀", mapOf(
                        EFF_COINS_PERCENT, -0.30, EFF_PRODUCTION_MULT, 0.15)));
                e.choices.add(new EventChoice("event_climate_adapt", "🌿", mapOf(
                        EFF_WELFARE, 5.0, EFF_STABILITY, 3.0)));
                break;

            case MARKET_CRASH:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.30);
                e.baseEffects.put(EFF_STRIKE_CHANCE, 0.10);
                e.choices.add(new EventChoice("event_crash_buylow", "📉", mapOf(
                        EFF_COINS_PERCENT, -0.25, EFF_PRODUCTION_MULT, 0.40)));
                e.choices.add(new EventChoice("event_crash_liquidate", "💸", mapOf(
                        EFF_COINS_PERCENT, 0.15, EFF_STABILITY, -5.0)));
                e.choices.add(new EventChoice("event_crash_wait", "⏳", mapOf(
                        EFF_STABILITY, 2.0)));
                break;

            case GALACTIC_FESTIVAL:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.10);
                e.baseEffects.put(EFF_MOOD_ALL, 1.0);
                e.choices.add(new EventChoice("event_festival_sponsor", "🎉", mapOf(
                        EFF_COINS_PERCENT, -0.08, EFF_STABILITY, 8.0, EFF_MOOD_ALL, 1.0)));
                e.choices.add(new EventChoice("event_festival_ignore", "🤷", mapOf(
                        EFF_STABILITY, -2.0)));
                break;

            case DIPLOMATIC_INCIDENT:
                e.baseEffects.put(EFF_STABILITY, -8.0);
                e.choices.add(new EventChoice("event_diplo_apologize", "🙇", mapOf(
                        EFF_STABILITY, 10.0, EFF_COINS_PERCENT, -0.05)));
                e.choices.add(new EventChoice("event_diplo_deny", "🚫", mapOf(
                        EFF_STABILITY, -12.0)));
                e.choices.add(new EventChoice("event_diplo_mediate", "⚖️", mapOf(
                        EFF_STABILITY, 5.0, EFF_COINS_PERCENT, -0.03)));
                break;

            case DARK_MATTER_SURGE:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.50);
                e.baseEffects.put(EFF_MUTATION_CHANCE, 0.20);
                e.baseEffects.put(EFF_STABILITY, 3.0);
                // No choices — cosmic windfall
                break;

            // ====== NEW WORLD_LOCAL EVENTS ======

            case ENERGY_BLACKOUT:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.35);
                e.choices.add(new EventChoice("event_blackout_generators", "⚡", mapOf(
                        EFF_COINS_PERCENT, -0.12, EFF_PRODUCTION_MULT, 0.30)));
                e.choices.add(new EventChoice("event_blackout_wait", "🕯️", mapOf(
                        EFF_STABILITY, -3.0)));
                break;

            case TERRAFORMING_SUCCESS:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.20);
                e.choices.add(new EventChoice("event_terra_expand", "🏗️", mapOf(
                        EFF_COINS_PERCENT, -0.15, EFF_PRODUCTION_MULT, 0.30, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_terra_sell", "💰", mapOf(
                        EFF_COINS_PERCENT, 0.25)));
                break;

            case SPACE_DEBRIS:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.15);
                e.choices.add(new EventChoice("event_debris_clean", "🧹", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_STABILITY, 5.0, EFF_PRODUCTION_MULT, 0.15)));
                e.choices.add(new EventChoice("event_debris_shield", "🛡️", mapOf(
                        EFF_COINS_PERCENT, -0.05)));
                e.choices.add(new EventChoice("event_debris_ignore", "🤷", mapOf(
                        EFF_PRODUCTION_MULT, -0.10, EFF_STABILITY, -5.0)));
                break;

            case CYBER_ATTACK:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.20);
                e.baseEffects.put(EFF_STABILITY, -5.0);
                e.choices.add(new EventChoice("event_cyber_firewall", "🔒", mapOf(
                        EFF_COINS_PERCENT, -0.15, EFF_STABILITY, 8.0)));
                e.choices.add(new EventChoice("event_cyber_counter", "💻", mapOf(
                        EFF_COINS_PERCENT, -0.08, EFF_PRODUCTION_MULT, 0.10)));
                e.choices.add(new EventChoice("event_cyber_pay", "💰", mapOf(
                        EFF_COINS_PERCENT, -0.20, EFF_PRODUCTION_MULT, 0.20)));
                break;

            case INNOVATION_GRANT:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.15);
                e.choices.add(new EventChoice("event_grant_research", "🔬", mapOf(
                        EFF_PRODUCTION_MULT, 0.25, EFF_MUTATION_CHANCE, 0.10)));
                e.choices.add(new EventChoice("event_grant_infrastructure", "🏭", mapOf(
                        EFF_PRODUCTION_MULT, 0.15, EFF_STABILITY, 5.0)));
                break;

            case SUPPLY_CHAIN_CRISIS:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.25);
                e.baseEffects.put(EFF_CONTRACT_COST, 0.20);
                e.choices.add(new EventChoice("event_supply_local", "🏪", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_PRODUCTION_MULT, 0.20, EFF_STABILITY, 3.0)));
                e.choices.add(new EventChoice("event_supply_stockpile", "📦", mapOf(
                        EFF_COINS_PERCENT, -0.20, EFF_PRODUCTION_MULT, 0.25)));
                e.choices.add(new EventChoice("event_supply_wait", "⏳", mapOf(
                        EFF_STABILITY, -5.0)));
                break;

            case MUTANT_OUTBREAK:
                e.baseEffects.put(EFF_WELFARE, -15.0);
                e.baseEffects.put(EFF_MUTATION_CHANCE, 0.25);
                e.choices.add(new EventChoice("event_mutant_quarantine", "🔬", mapOf(
                        EFF_WELFARE, 10.0, EFF_COINS_PERCENT, -0.10, EFF_MUTATION_CHANCE, -0.15)));
                e.choices.add(new EventChoice("event_mutant_study", "🧪", mapOf(
                        EFF_MUTATION_CHANCE, 0.30, EFF_STABILITY, -5.0)));
                break;

            // ====== NEW CORPORATE EVENTS ======

            case TAX_AUDIT:
                e.baseEffects.put(EFF_STABILITY, -3.0);
                e.choices.add(new EventChoice("event_tax_comply", "📋", mapOf(
                        EFF_COINS_PERCENT, -0.15, EFF_STABILITY, 8.0)));
                e.choices.add(new EventChoice("event_tax_offshore", "🏝️", mapOf(
                        EFF_COINS_PERCENT, -0.05, EFF_STABILITY, -10.0)));
                e.choices.add(new EventChoice("event_tax_negotiate", "🤝", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_STABILITY, 3.0)));
                break;

            case WORKER_TRAINING:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.05);
                e.choices.add(new EventChoice("event_training_full", "🎓", mapOf(
                        EFF_COINS_PERCENT, -0.12, EFF_PRODUCTION_MULT, 0.30, EFF_MOOD_ALL, 1.0)));
                e.choices.add(new EventChoice("event_training_partial", "📖", mapOf(
                        EFF_COINS_PERCENT, -0.05, EFF_PRODUCTION_MULT, 0.10)));
                e.choices.add(new EventChoice("event_training_skip", "❌", mapOf(
                        EFF_MOOD_ALL, -1.0, EFF_STABILITY, -3.0)));
                break;

            case ESPIONAGE:
                e.baseEffects.put(EFF_STABILITY, -5.0);
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.10);
                e.choices.add(new EventChoice("event_spy_counterintel", "🕵️", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_STABILITY, 10.0)));
                e.choices.add(new EventChoice("event_spy_doubleagent", "🎭", mapOf(
                        EFF_PRODUCTION_MULT, 0.15, EFF_STABILITY, -8.0)));
                break;

            case LABOR_SHORTAGE:
                e.baseEffects.put(EFF_PRODUCTION_MULT, -0.20);
                e.baseEffects.put(EFF_CONTRACT_COST, 0.25);
                e.choices.add(new EventChoice("event_labor_raise", "💰", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_MOOD_ALL, 1.0, EFF_PRODUCTION_MULT, 0.15)));
                e.choices.add(new EventChoice("event_labor_automate", "🤖", mapOf(
                        EFF_COINS_PERCENT, -0.20, EFF_PRODUCTION_MULT, 0.25, EFF_MOOD_ALL, -1.0)));
                break;

            case PET_COMPETITION:
                e.baseEffects.put(EFF_WELFARE, 5.0);
                e.choices.add(new EventChoice("event_petcomp_enter", "🏅", mapOf(
                        EFF_WELFARE, 15.0, EFF_COINS_PERCENT, -0.05, EFF_STABILITY, 5.0)));
                e.choices.add(new EventChoice("event_petcomp_host", "🎪", mapOf(
                        EFF_COINS_PERCENT, -0.10, EFF_STABILITY, 8.0, EFF_MOOD_ALL, 1.0)));
                e.choices.add(new EventChoice("event_petcomp_skip", "🤷", mapOf(
                        EFF_WELFARE, -5.0)));
                break;

            case INVESTOR_INTEREST:
                e.baseEffects.put(EFF_PRODUCTION_MULT, 0.10);
                e.choices.add(new EventChoice("event_investor_accept", "🤝", mapOf(
                        EFF_COINS_PERCENT, 0.20, EFF_PRODUCTION_MULT, 0.20, EFF_STABILITY, -5.0)));
                e.choices.add(new EventChoice("event_investor_decline", "🚫", mapOf(
                        EFF_STABILITY, 5.0)));
                break;
        }
        return e;
    }

    // Helper
    private static Map<String, Double> mapOf(Object... pairs) {
        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put((String) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        }
        return map;
    }
}
