package com.example.juego;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Sistema de Mascotas con salud, enfermedades, genética y cría
 */
public class Pet implements Serializable {

    // ========================== TRAIT SYSTEM ==========================

    public enum PetTrait {
        // Common traits (max +6%)
        SWIFT("⚡", "Swift", BonusType.TAP_POWER, 0.05, false),
        HARDY("🛡️", "Hardy", BonusType.PRODUCTION, 0.06, false),
        CLEVER("🧠", "Clever", BonusType.CRIT_CHANCE, 0.03, false),
        CHEERFUL("😊", "Cheerful", BonusType.PRODUCTION, 0.04, false),
        RESILIENT("💪", "Resilient", BonusType.PRODUCTION, 0.05, false),
        PLAYFUL("🎮", "Playful", BonusType.COOLDOWN_REDUCTION, 0.05, false),
        CURIOUS("🔍", "Curious", BonusType.TAP_POWER, 0.04, false),
        CALM("🧘", "Calm", BonusType.OFFLINE_BONUS, 0.06, false),
        LOYAL("❤️", "Loyal", BonusType.PRODUCTION, 0.05, false),
        ENERGETIC("🔋", "Energetic", BonusType.TAP_POWER, 0.06, false),
        // Rare traits (max +8%, only from mutations)
        LUMINOUS("✨", "Luminous", BonusType.GLOBAL_MULT, 0.07, true),
        MYSTIC("🔮", "Mystic", BonusType.GLOBAL_MULT, 0.08, true),
        CELESTIAL("🌟", "Celestial", BonusType.ALL_SMALL, 0.04, true),
        VOID_TOUCHED("🌑", "Void-Touched", BonusType.MEGA_MULT, 0.06, true),
        QUANTUM("⚛️", "Quantum", BonusType.CRIT_CHANCE, 0.05, true);

        public final String emoji;
        public final String name;
        public final BonusType bonusType;
        public final double bonusValue;
        public final boolean rare;

        PetTrait(String emoji, String name, BonusType bonusType, double bonusValue, boolean rare) {
            this.emoji = emoji;
            this.name = name;
            this.bonusType = bonusType;
            this.bonusValue = bonusValue;
            this.rare = rare;
        }

        public static PetTrait[] commonTraits() {
            return Arrays.stream(values()).filter(t -> !t.rare).toArray(PetTrait[]::new);
        }

        public static PetTrait[] rareTraits() {
            return Arrays.stream(values()).filter(t -> t.rare).toArray(PetTrait[]::new);
        }
    }

    public enum PetType {
        ROBO_CAT("🐱", "Robo-Cat", "Aumenta producción pasiva",
                BonusType.PRODUCTION, 0.05, 5000),
        CYBER_DOG("🐕", "Cyber-Dog", "Aumenta coins por tap",
                BonusType.TAP_POWER, 0.1, 10000),
        QUANTUM_BIRD("🐦", "Quantum Bird", "Aumenta probabilidad de crítico",
                BonusType.CRIT_CHANCE, 0.02, 25000),
        NANO_BUNNY("🐰", "Nano-Bunny", "Reduce tiempo de cooldown",
                BonusType.COOLDOWN_REDUCTION, 0.1, 50000),
        HOLO_DRAGON("🐉", "Holo-Dragon", "Multiplicador global",
                BonusType.GLOBAL_MULT, 0.08, 250000),
        ASTRAL_PHOENIX("🦅", "Astral Phoenix", "Bonus a ganancias offline",
                BonusType.OFFLINE_BONUS, 0.15, 1000000),
        COSMIC_UNICORN("🦄", "Cosmic Unicorn", "Todos los bonus pequeños",
                BonusType.ALL_SMALL, 0.03, 5000000),
        INFINITY_WHALE("🐋", "Infinity Whale", "Mega multiplicador",
                BonusType.MEGA_MULT, 0.25, 50000000),
        NEBULA_FOX("🦊", "Nebula Fox", "Bonus a minijuegos",
                BonusType.COOLDOWN_REDUCTION, 0.15, 500000000),
        VOID_SERPENT("🐍", "Void Serpent", "Absorbe monedas del vacío",
                BonusType.PRODUCTION, 0.20, 5000000000L),
        CHRONO_OWL("🦉", "Chrono Owl", "Manipula el tiempo de combos",
                BonusType.GLOBAL_MULT, 0.12, 50000000000L),
        STELLAR_JELLYFISH("🪼", "Stellar Jellyfish", "Genera electricidad cósmica",
                BonusType.ALL_SMALL, 0.06, 500000000000L),
        // === LEGENDARY TIER ===
        TITAN_LEVIATHAN("🐙", "Titan Leviathan", "Devorador de dimensiones",
                BonusType.MEGA_MULT, 0.40, 5000000000000L),
        ETHEREAL_GRIFFIN("🦁", "Ethereal Griffin", "Guardián del multiverso",
                BonusType.GLOBAL_MULT, 0.25, 50000000000000L),
        DIMENSIONAL_KRAKEN("🦑", "Dimensional Kraken", "Tentáculos entre realidades",
                BonusType.ALL_SMALL, 0.15, 500000000000000L),
        PRIMORDIAL_HYDRA("🐲", "Primordial Hydra", "Cada cabeza genera riqueza",
                BonusType.PRODUCTION, 0.50, 5000000000000000L),
        CELESTIAL_SPHINX("🐈\u200D⬛", "Celestial Sphinx", "Acertijos que multiplican",
                BonusType.TAP_POWER, 0.40, 50000000000000000L),
        OMEGA_CHIMERA("🔥", "Omega Chimera", "La fusión definitiva",
                BonusType.MEGA_MULT, 1.0, 1000000000000000000L);

        public final String emoji;
        public final String name;
        public final String description;
        public final BonusType bonusType;
        public final double bonusValue;
        public final double baseCost;

        PetType(String emoji, String name, String description,
                BonusType bonusType, double bonusValue, double baseCost) {
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.bonusType = bonusType;
            this.bonusValue = bonusValue;
            this.baseCost = baseCost;
        }
    }

    public enum BonusType {
        PRODUCTION, TAP_POWER, CRIT_CHANCE, COOLDOWN_REDUCTION,
        GLOBAL_MULT, OFFLINE_BONUS, ALL_SMALL, MEGA_MULT
    }

    public enum PetMood {
        ECSTATIC("🤩", 1.5, "¡Extasiado!"),
        HAPPY("😊", 1.2, "Feliz"),
        NORMAL("😐", 1.0, "Normal"),
        SAD("😢", 0.7, "Triste"),
        DEPRESSED("😭", 0.4, "Deprimido"),
        ANGRY("😠", 0.5, "Enfadado"),
        SICK("🤢", 0.3, "Enfermo"),
        DYING("💀", 0.1, "Moribundo");

        public final String emoji;
        public final double effectivenessMultiplier;
        public final String description;

        PetMood(String emoji, double mult, String desc) {
            this.emoji = emoji;
            this.effectivenessMultiplier = mult;
            this.description = desc;
        }
    }

    public enum Disease {
        NONE("", "", 0, 0),
        COLD("🤧", "Resfriado", 500, 3),
        FLU("🤒", "Gripe", 2000, 5),
        INFECTION("🦠", "Infección", 5000, 7),
        DEPRESSION("😶", "Depresión", 3000, 10),
        ANXIETY("😰", "Ansiedad", 2500, 6),
        LONELINESS("💔", "Soledad", 1000, 4),
        MALNUTRITION("🍽️", "Desnutrición", 1500, 5),
        EXHAUSTION("😵", "Agotamiento", 2000, 4),
        VIRUS("☣️", "Virus crítico", 10000, 10);

        public final String emoji;
        public final String name;
        public final double treatmentCost;
        public final int severityDays; // Días para volverse grave

        Disease(String emoji, String name, double cost, int days) {
            this.emoji = emoji;
            this.name = name;
            this.treatmentCost = cost;
            this.severityDays = days;
        }
    }

    private PetType type;
    private String customName;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private boolean owned;
    private boolean alive;
    private boolean isGhost; // Después de morir, puede convertirse en fantasma
    private PetMood mood;

    // Estadísticas vitales
    private int health;        // 0-100 (salud física)
    private int happiness;     // 0-100 (felicidad)
    private int hunger;        // 0-100 (100 = muy hambriento)
    private int energy;        // 0-100
    private int mentalHealth;  // 0-100 (salud mental)
    private int hygiene;       // 0-100 (limpieza)

    // Enfermedades
    private List<Disease> diseases;
    private long diseaseStartTime;

    // Tiempos
    private long lastFedTime;
    private long lastPetTime;
    private long lastPlayTime;
    private long lastCleanTime;
    private long lastCheckTime;
    private long birthTime;
    private long deathTime;

    // Estadísticas de vida
    private int timesFed;
    private int timesPetted;
    private int timesPlayed;
    private int diseasesCured;

    // === GENETIC / BREEDING ===
    private String petId;
    private List<PetTrait> traits;      // max 2 active traits
    private int generation;             // 0 = purchased, n+1 = bred
    private boolean hybrid;
    private String parentId1;
    private String parentId2;
    private long lastBreedTime;         // cooldown tracking

    private static final Random random = new Random();
    public static final int MAX_PETS = 15;
    public static final long BREED_COOLDOWN_MS = 3600000; // 1 hour
    public static final double MUTATION_CHANCE = 0.10; // 10%
    public static final long INCUBATION_MIN_MS = 48L * 3600 * 1000; // 48 hours min
    public static final long INCUBATION_MAX_MS = 72L * 3600 * 1000; // 72 hours max

    // Incubation state (for bred offspring not yet hatched)
    private boolean incubating;
    private long incubationEndTime;
    private boolean hadMutation; // track if this offspring had a rare mutation

    public Pet(PetType type) {
        this.petId = UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.customName = type.name;
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.owned = false;
        this.alive = true;
        this.isGhost = false;
        this.mood = PetMood.NORMAL;

        // Estadísticas vitales iniciales
        this.health = 100;
        this.happiness = 70;
        this.hunger = 20;
        this.energy = 80;
        this.mentalHealth = 80;
        this.hygiene = 90;

        this.diseases = new ArrayList<>();

        this.lastFedTime = System.currentTimeMillis();
        this.lastPetTime = 0;
        this.lastPlayTime = 0;
        this.lastCleanTime = System.currentTimeMillis();
        this.lastCheckTime = System.currentTimeMillis();
        this.birthTime = System.currentTimeMillis();

        this.timesFed = 0;
        this.timesPetted = 0;
        this.timesPlayed = 0;
        this.diseasesCured = 0;

        // Genetics — assign 2 random common traits
        this.traits = new ArrayList<>();
        this.generation = 0;
        this.hybrid = false;
        this.parentId1 = null;
        this.parentId2 = null;
        this.lastBreedTime = 0;
        this.incubating = false;
        this.incubationEndTime = 0;
        this.hadMutation = false;
        assignRandomTraits(2, false);
    }

    /**
     * Actualiza el estado de la mascota (llamar periódicamente)
     */
    public void update() {
        if (!owned || !alive) return;

        long now = System.currentTimeMillis();
        long hoursSinceLastCheck = (now - lastCheckTime) / 3600000;

        if (hoursSinceLastCheck >= 1) {
            // Actualizar hambre
            long hoursSinceLastFed = (now - lastFedTime) / 3600000;
            hunger = Math.min(100, (int)(hoursSinceLastFed * 5));

            // Actualizar energía
            long hoursSinceLastPlay = lastPlayTime > 0 ? (now - lastPlayTime) / 3600000 : 24;
            energy = Math.max(0, 100 - (int)(hoursSinceLastPlay * 3));

            // Actualizar higiene
            long hoursSinceLastClean = (now - lastCleanTime) / 3600000;
            hygiene = Math.max(0, 100 - (int)(hoursSinceLastClean * 4));

            // Actualizar salud mental basada en interacciones
            long hoursSinceLastPet = lastPetTime > 0 ? (now - lastPetTime) / 3600000 : 48;
            if (hoursSinceLastPet > 12) {
                mentalHealth = Math.max(0, mentalHealth - (int)((hoursSinceLastPet - 12) * 2));
            }

            // Verificar enfermedades
            checkForDiseases();

            // Actualizar salud física
            updateHealth();

            // Actualizar humor
            updateMood();

            // Verificar muerte
            checkDeath();

            lastCheckTime = now;
        }
    }

    private void checkForDiseases() {
        if (diseases.size() >= 3) return; // Máximo 3 enfermedades simultáneas

        // Probabilidad de enfermarse basada en estadísticas
        double sickChance = 0;

        if (hunger > 70) sickChance += 0.1;
        if (hygiene < 30) sickChance += 0.15;
        if (mentalHealth < 40) sickChance += 0.1;
        if (health < 50) sickChance += 0.1;
        if (energy < 20) sickChance += 0.05;

        if (random.nextDouble() < sickChance) {
            Disease newDisease = generateDisease();
            if (newDisease != Disease.NONE && !diseases.contains(newDisease)) {
                diseases.add(newDisease);
                if (diseaseStartTime == 0) {
                    diseaseStartTime = System.currentTimeMillis();
                }
            }
        }
    }

    private Disease generateDisease() {
        // Enfermedades físicas
        if (hunger > 80) return Disease.MALNUTRITION;
        if (hygiene < 20) return random.nextBoolean() ? Disease.INFECTION : Disease.COLD;
        if (energy < 15) return Disease.EXHAUSTION;

        // Enfermedades mentales
        if (mentalHealth < 30) {
            int roll = random.nextInt(3);
            if (roll == 0) return Disease.DEPRESSION;
            if (roll == 1) return Disease.ANXIETY;
            return Disease.LONELINESS;
        }

        // Enfermedades aleatorias si salud baja
        if (health < 40) {
            return random.nextDouble() < 0.3 ? Disease.FLU : Disease.COLD;
        }

        // Virus raro
        if (random.nextDouble() < 0.01) return Disease.VIRUS;

        return Disease.NONE;
    }

    private void updateHealth() {
        int healthDrain = 0;

        // Hambre drena salud
        if (hunger > 80) healthDrain += 5;
        else if (hunger > 60) healthDrain += 2;

        // Enfermedades drenan salud
        for (Disease d : diseases) {
            healthDrain += 3;
        }

        // Mala higiene drena salud
        if (hygiene < 20) healthDrain += 2;

        // Salud mental muy baja afecta salud física
        if (mentalHealth < 20) healthDrain += 2;

        health = Math.max(0, health - healthDrain);

        // Recuperación si está bien cuidado
        if (hunger < 30 && hygiene > 70 && diseases.isEmpty() && energy > 50) {
            health = Math.min(100, health + 3);
        }
    }

    public void updateMood() {
        if (!alive) {
            mood = PetMood.DYING;
            return;
        }

        // Si tiene enfermedades graves
        if (!diseases.isEmpty()) {
            boolean hasPhysical = diseases.stream().anyMatch(d ->
                d == Disease.VIRUS || d == Disease.INFECTION || d == Disease.FLU);
            boolean hasMental = diseases.stream().anyMatch(d ->
                d == Disease.DEPRESSION || d == Disease.ANXIETY || d == Disease.LONELINESS);

            if (hasPhysical) {
                mood = PetMood.SICK;
                return;
            }
            if (hasMental) {
                mood = PetMood.DEPRESSED;
                return;
            }
        }

        // Basado en estadísticas generales
        int overallWellbeing = (health + happiness + mentalHealth + (100 - hunger) + energy + hygiene) / 6;

        if (overallWellbeing >= 90) mood = PetMood.ECSTATIC;
        else if (overallWellbeing >= 70) mood = PetMood.HAPPY;
        else if (overallWellbeing >= 50) mood = PetMood.NORMAL;
        else if (overallWellbeing >= 30) mood = PetMood.SAD;
        else if (overallWellbeing >= 15) mood = PetMood.DEPRESSED;
        else mood = PetMood.DYING;
    }

    private void checkDeath() {
        if (health <= 0) {
            die("salud agotada");
        } else if (hunger >= 100 && health < 20) {
            die("inanición");
        } else if (mentalHealth <= 0 && health < 30) {
            die("corazón roto");
        } else if (diseases.size() >= 3 && health < 15) {
            die("múltiples enfermedades");
        }
    }

    private void die(String cause) {
        alive = false;
        deathTime = System.currentTimeMillis();
        mood = PetMood.DYING;
        // Puede convertirse en fantasma que da pequeño bonus
        isGhost = random.nextDouble() < 0.3;
    }

    // === ACCIONES DEL JUGADOR ===

    public boolean feed(double foodCost, double availableCoins) {
        if (!alive || availableCoins < foodCost) return false;

        lastFedTime = System.currentTimeMillis();
        hunger = Math.max(0, hunger - 40);
        happiness = Math.min(100, happiness + 15);
        health = Math.min(100, health + 5);
        experience += 10;
        timesFed++;

        // Curar desnutrición
        diseases.remove(Disease.MALNUTRITION);

        updateMood();
        checkLevelUp();
        return true;
    }

    public boolean pet() {
        if (!alive) return false;
        long now = System.currentTimeMillis();
        if (now - lastPetTime < 1800000) return false; // 30 min cooldown

        lastPetTime = now;
        happiness = Math.min(100, happiness + 20);
        mentalHealth = Math.min(100, mentalHealth + 15);
        experience += 5;
        timesPetted++;

        // Curar soledad
        diseases.remove(Disease.LONELINESS);

        updateMood();
        checkLevelUp();
        return true;
    }

    public boolean play() {
        if (!alive || energy < 10) return false;

        lastPlayTime = System.currentTimeMillis();
        happiness = Math.min(100, happiness + 25);
        mentalHealth = Math.min(100, mentalHealth + 10);
        energy = Math.max(0, energy - 20);
        experience += 15;
        timesPlayed++;

        // Curar depresión/ansiedad con probabilidad
        if (random.nextDouble() < 0.3) {
            diseases.remove(Disease.DEPRESSION);
            diseases.remove(Disease.ANXIETY);
        }

        updateMood();
        checkLevelUp();
        return true;
    }

    public boolean clean(double cost, double availableCoins) {
        if (!alive || availableCoins < cost) return false;

        lastCleanTime = System.currentTimeMillis();
        hygiene = 100;
        health = Math.min(100, health + 5);
        experience += 5;

        // Curar infección con probabilidad
        if (random.nextDouble() < 0.5) {
            diseases.remove(Disease.INFECTION);
            diseases.remove(Disease.COLD);
        }

        updateMood();
        return true;
    }

    public boolean treatDisease(Disease disease, double availableCoins) {
        if (!alive || !diseases.contains(disease)) return false;
        if (availableCoins < disease.treatmentCost) return false;

        diseases.remove(disease);
        health = Math.min(100, health + 20);
        diseasesCured++;

        if (diseases.isEmpty()) {
            diseaseStartTime = 0;
        }

        updateMood();
        return true;
    }

    public boolean rest() {
        if (!alive) return false;

        energy = Math.min(100, energy + 30);
        if (energy > 80) {
            diseases.remove(Disease.EXHAUSTION);
        }

        updateMood();
        return true;
    }

    /**
     * Revivir mascota (muy caro)
     */
    public boolean revive(double cost, double availableCoins) {
        if (alive || availableCoins < cost) return false;

        alive = true;
        isGhost = false;
        health = 50;
        happiness = 50;
        hunger = 50;
        energy = 50;
        mentalHealth = 50;
        hygiene = 50;
        diseases.clear();
        mood = PetMood.NORMAL;

        return true;
    }

    public double getReviveCost() {
        return type.baseCost * 5;
    }

    private void checkLevelUp() {
        while (experience >= experienceToNextLevel && level < 50) {
            experience -= experienceToNextLevel;
            level++;
            experienceToNextLevel = 100 * Math.pow(1.5, level - 1);
            // Subir de nivel da bonus de salud
            health = Math.min(100, health + 10);
            happiness = Math.min(100, happiness + 10);
        }
    }

    public double getCurrentBonus() {
        if (!owned) return 0;
        if (!alive) return isGhost ? type.bonusValue * 0.1 : 0;

        double baseBonus = type.bonusValue * level;
        double healthMod = health / 100.0;
        double moodMod = mood.effectivenessMultiplier;

        return baseBonus * healthMod * moodMod;
    }

    public String getStatusEmoji() {
        if (!alive) return isGhost ? "👻" : "💀";
        if (!diseases.isEmpty()) return diseases.get(0).emoji;
        return mood.emoji;
    }

    public String getFullStatus() {
        if (!alive) {
            return isGhost ? "👻 Fantasma" : "💀 Fallecido";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(mood.emoji).append(" ").append(mood.description);

        if (!diseases.isEmpty()) {
            sb.append("\n🏥 Enfermedades: ");
            for (Disease d : diseases) {
                sb.append(d.emoji).append(d.name).append(" ");
            }
        }

        return sb.toString();
    }

    public double getPurchaseCost() {
        return type.baseCost;
    }

    public double getFeedCost(double currentProduction) {
        return Math.max(100, currentProduction * 10);
    }

    public double getCleanCost(double currentProduction) {
        return Math.max(50, currentProduction * 5);
    }

    public boolean canPet() {
        return alive && System.currentTimeMillis() - lastPetTime >= 1800000;
    }

    public long getPetCooldownRemaining() {
        long remaining = 1800000 - (System.currentTimeMillis() - lastPetTime);
        return Math.max(0, remaining / 1000);
    }

    public boolean canPlay() {
        return alive && energy >= 10;
    }

    public int getAge() {
        long ageMs = (alive ? System.currentTimeMillis() : deathTime) - birthTime;
        return (int)(ageMs / (24 * 60 * 60 * 1000)); // Días
    }

    // Getters
    public PetType getType() { return type; }
    public String getCustomName() { return customName; }
    public int getLevel() { return level; }
    public double getExperience() { return experience; }
    public double getExperienceToNextLevel() { return experienceToNextLevel; }
    public boolean isOwned() { return owned; }
    public boolean isAlive() { return alive; }
    public boolean isGhost() { return isGhost; }
    public PetMood getMood() { return mood; }
    public int getHealth() { return health; }
    public int getHappiness() { return happiness; }
    public int getHunger() { return hunger; }
    public int getEnergy() { return energy; }
    public int getMentalHealth() { return mentalHealth; }
    public int getHygiene() { return hygiene; }
    public List<Disease> getDiseases() { return diseases; }
    public long getLastFedTime() { return lastFedTime; }

    // Setters
    public void setOwned(boolean owned) {
        this.owned = owned;
        if (owned && birthTime == 0) {
            birthTime = System.currentTimeMillis();
            lastFedTime = System.currentTimeMillis();
            lastCleanTime = System.currentTimeMillis();
        }
    }
    public void setCustomName(String name) { this.customName = name; }
    public void setLevel(int level) { this.level = level; }
    public void setExperience(double exp) { this.experience = exp; }
    public void setHealth(int health) { this.health = health; }
    public void setHappiness(int happiness) { this.happiness = happiness; }
    public void setHunger(int hunger) { this.hunger = hunger; }
    public void setEnergy(int energy) { this.energy = energy; }
    public void setMentalHealth(int mh) { this.mentalHealth = mh; }
    public void setHygiene(int hygiene) { this.hygiene = hygiene; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public void setGhost(boolean ghost) { this.isGhost = ghost; }
    public void setMood(PetMood mood) { this.mood = mood; }
    public void setDiseases(List<Disease> diseases) { this.diseases = diseases != null ? diseases : new ArrayList<>(); }
    public void setBirthTime(long time) { this.birthTime = time; }
    public void setDeathTime(long time) { this.deathTime = time; }
    public void setExperienceToNextLevel(double xp) { this.experienceToNextLevel = xp; }
    public void setLastFedTime(long time) { this.lastFedTime = time; }
    public void setLastPetTime(long time) { this.lastPetTime = time; }
    public void setLastPlayTime(long time) { this.lastPlayTime = time; }
    public void setLastCleanTime(long time) { this.lastCleanTime = time; }
    public void setLastCheckTime(long time) { this.lastCheckTime = time; }
    public long getBirthTime() { return birthTime; }
    public long getDeathTime() { return deathTime; }
    public long getLastPlayTime() { return lastPlayTime; }
    public long getLastCleanTime() { return lastCleanTime; }
    public long getLastCheckTime() { return lastCheckTime; }
    public long getLastPetTime() { return lastPetTime; }

    // === GENETIC GETTERS/SETTERS ===
    public String getPetId() { return petId != null ? petId : "legacy"; }
    public void setPetId(String id) { this.petId = id; }
    public List<PetTrait> getTraits() { return traits != null ? traits : new ArrayList<>(); }
    public void setTraits(List<PetTrait> t) { this.traits = t != null ? t : new ArrayList<>(); }
    public int getGeneration() { return generation; }
    public void setGeneration(int g) { this.generation = g; }
    public boolean isHybrid() { return hybrid; }
    public void setHybrid(boolean h) { this.hybrid = h; }
    public String getParentId1() { return parentId1; }
    public void setParentId1(String id) { this.parentId1 = id; }
    public String getParentId2() { return parentId2; }
    public void setParentId2(String id) { this.parentId2 = id; }
    public long getLastBreedTime() { return lastBreedTime; }
    public void setLastBreedTime(long t) { this.lastBreedTime = t; }

    // Incubation
    public boolean isIncubating() { return incubating; }
    public void setIncubating(boolean b) { this.incubating = b; }
    public long getIncubationEndTime() { return incubationEndTime; }
    public void setIncubationEndTime(long t) { this.incubationEndTime = t; }
    public boolean hadMutation() { return hadMutation; }
    public void setHadMutation(boolean m) { this.hadMutation = m; }

    /** Check if incubation is complete */
    public boolean isReadyToHatch() {
        return incubating && System.currentTimeMillis() >= incubationEndTime;
    }

    /** Get remaining incubation time in seconds */
    public long getIncubationRemaining() {
        if (!incubating) return 0;
        return Math.max(0, (incubationEndTime - System.currentTimeMillis()) / 1000);
    }

    /** Hatch the pet (call when ready) */
    public void hatch() {
        incubating = false;
        birthTime = System.currentTimeMillis();
    }

    public boolean canBreed() {
        return alive && owned && !isGhost &&
                System.currentTimeMillis() - lastBreedTime >= BREED_COOLDOWN_MS;
    }

    public long getBreedCooldownRemaining() {
        long remaining = BREED_COOLDOWN_MS - (System.currentTimeMillis() - lastBreedTime);
        return Math.max(0, remaining / 1000);
    }

    /** Assign N random common traits (used at creation) */
    private void assignRandomTraits(int count, boolean allowRare) {
        if (traits == null) traits = new ArrayList<>();
        PetTrait[] pool = allowRare ? PetTrait.values() : PetTrait.commonTraits();
        Set<PetTrait> chosen = new HashSet<>(traits);
        int attempts = 0;
        while (chosen.size() < count && attempts < 50) {
            PetTrait t = pool[random.nextInt(pool.length)];
            chosen.add(t);
            attempts++;
        }
        traits = new ArrayList<>(chosen);
        if (traits.size() > 2) traits = new ArrayList<>(traits.subList(0, 2));
    }

    /**
     * Calculate total trait bonus for a specific BonusType.
     * First trait gives 100%, second gives 70%.
     * Generation cap: max total = 0.30 + generation * 0.04 (cap 0.50)
     */
    public double getTraitBonus(BonusType forType) {
        if (traits == null || traits.isEmpty()) return 0;
        double total = 0;
        int matchCount = 0;
        for (PetTrait trait : traits) {
            if (trait.bonusType == forType) {
                matchCount++;
                double mult = matchCount == 1 ? 1.0 : 0.7; // stacking penalty
                total += trait.bonusValue * mult;
            }
        }
        // Generation cap
        double maxBonus = Math.min(0.50, 0.30 + generation * 0.04);
        return Math.min(total, maxBonus);
    }

    /** Total effective bonus = type bonus + trait bonus, modulated by mood */
    public double getTotalBonus(BonusType forType) {
        double typeBonus = (type.bonusType == forType) ? type.bonusValue : 0;
        double traitBonus = getTraitBonus(forType);
        return (typeBonus + traitBonus) * mood.effectivenessMultiplier;
    }

    /**
     * Breed two pets to create an offspring.
     * Inherits 2 random traits from the 4 parental traits, + 10% mutation chance.
     */
    public static Pet breed(Pet parent1, Pet parent2) {
        // Offspring uses the type of the parent with higher bonusValue
        PetType offType = parent1.type.bonusValue >= parent2.type.bonusValue ? parent1.type : parent2.type;

        Pet offspring = new Pet(offType);
        offspring.petId = UUID.randomUUID().toString().substring(0, 8);
        offspring.owned = true;
        offspring.hybrid = true;
        offspring.generation = Math.max(parent1.generation, parent2.generation) + 1;
        offspring.parentId1 = parent1.getPetId();
        offspring.parentId2 = parent2.getPetId();
        offspring.customName = "🧬 " + offType.name + " G" + offspring.generation;

        // Incubation: 48-72 hours random
        offspring.incubating = true;
        long incubationDuration = INCUBATION_MIN_MS + (long)(random.nextDouble() * (INCUBATION_MAX_MS - INCUBATION_MIN_MS));
        offspring.incubationEndTime = System.currentTimeMillis() + incubationDuration;

        // Collect all parental traits (up to 4)
        List<PetTrait> parentalTraits = new ArrayList<>();
        if (parent1.traits != null) parentalTraits.addAll(parent1.traits);
        if (parent2.traits != null) parentalTraits.addAll(parent2.traits);

        // Remove duplicates, shuffle, pick 2
        List<PetTrait> uniqueParental = new ArrayList<>(new HashSet<>(parentalTraits));
        Collections.shuffle(uniqueParental, random);
        List<PetTrait> inherited = new ArrayList<>();
        for (int i = 0; i < Math.min(2, uniqueParental.size()); i++) {
            inherited.add(uniqueParental.get(i));
        }

        // Mutation chance: replace one trait with a rare one (base + event modifier)
        offspring.hadMutation = false;
        double mutationChance = MUTATION_CHANCE;
        try { mutationChance += GameState.getInstance().getEventMutationModifier(); } catch (Exception ignored) {}
        mutationChance = Math.max(0, Math.min(0.5, mutationChance)); // cap at 50%
        if (random.nextDouble() < mutationChance) {
            PetTrait[] rares = PetTrait.rareTraits();
            PetTrait mutation = rares[random.nextInt(rares.length)];
            if (inherited.size() >= 2) {
                inherited.set(random.nextInt(2), mutation); // replace one
            } else {
                inherited.add(mutation);
            }
            offspring.hadMutation = true;
        }

        offspring.traits = inherited;

        // Set breed cooldown on parents
        long now = System.currentTimeMillis();
        parent1.lastBreedTime = now;
        parent2.lastBreedTime = now;

        return offspring;
    }

    /** Get breeding cost based on parent costs */
    public static double getBreedCost(Pet parent1, Pet parent2) {
        return (parent1.type.baseCost + parent2.type.baseCost) * 0.5 *
                (1 + Math.max(parent1.generation, parent2.generation) * 0.3);
    }
}
