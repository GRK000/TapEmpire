package com.example.juego;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TAP EMPIRE - Estado central del juego
 * Sistema completo con Mundos, Minijuegos, Misiones, Mascotas, Eventos y Combos
 */
public class GameState {
    private static final GameState instance = new GameState();

    // === RECURSOS PRINCIPALES ===
    private double coins = 0;
    private double totalCoinsEarned = 0;
    private double prestigePoints = 0;
    private int prestigeLevel = 0;

    // === ESTADÍSTICAS DE TAPPING ===
    private long totalTaps = 0;
    private long totalCriticalHits = 0;
    private double basePerTap = 1;
    private double tapMultiplier = 1.0;
    private double criticalChance = 0.05;
    private double criticalMultiplier = 2.0;

    // === PRODUCCIÓN PASIVA ===
    private double productionMultiplier = 1.0;
    private double offlineMultiplier = 0.5;
    private long lastUpdateTime;

    // === NUEVOS SISTEMAS ===
    private List<World> worlds;
    private int currentWorldIndex = 0;
    private List<MiniGame> miniGames;
    private List<DailyMission> dailyMissions;
    private long lastMissionResetTime;
    private List<Pet> pets;
    private Pet activePet;
    private SpecialEvent activeEvent;
    private long lastEventTime;
    private ComboSystem comboSystem;

    // === ESTADÍSTICAS ADICIONALES ===
    private int totalGeneratorsBought = 0;
    private int totalMiniGamesPlayed = 0;
    private int totalEventsCompleted = 0;

    // === ESTADÍSTICAS GLOBALES (nunca se resetean) ===
    private double globalTotalCoinsEarned = 0;
    private long globalTotalTaps = 0;
    private int globalTotalGeneratorsBought = 0;
    private int totalPrestigesPerformed = 0;
    private long globalTotalCriticalHits = 0;

    // === GENERADORES, MEJORAS, LOGROS ===
    private List<Generator> generators;
    private List<Upgrade> upgrades;
    private List<Achievement> achievements;
    private List<Achievement> recentlyUnlocked;

    // === SISTEMA DE TRABAJADORES ===
    private List<Worker> ownedWorkers;
    private List<Worker.ContractTemplate> contractTemplates;
    private long lastSalaryPayTime;
    private static final long SALARY_INTERVAL_MS = 5 * 60 * 1000; // 5 min real-time = 1 "month"
    private double totalMonthlySalary = 0;

    // === NURSERY / BREEDING ===
    private double nurseryWelfareLevel = 70.0; // 0-100, affects all pet bonuses
    private long lastWelfareDecayTime = System.currentTimeMillis();
    public static final String NURSERY_ASSIGNMENT_ID = "NURSERY"; // special worker assignment

    // Tracking counters for achievements
    private int totalPetsBred = 0;
    private int totalRareMutations = 0;
    private int totalStrikesResolved = 0;
    private int maxComboReached = 0;

    // === EVENT SYSTEM ===
    private double galacticStability = 50.0;  // 0-100 index
    private List<GameEvent> activeGameEvents = new ArrayList<>();
    private List<GameEvent> eventHistory = new ArrayList<>();
    private long lastMinorEventTime = 0;
    private long lastMajorEventTime = 0;
    private int eventCheckCounter = 0;
    private static final long MINOR_EVENT_COOLDOWN = 600_000;  // 10 min
    private static final long MAJOR_EVENT_COOLDOWN = 2_700_000; // 45 min
    private static final int MAX_ACTIVE_EVENTS = 3;
    private List<GameEvent> lastAutoResolvedEvents = new ArrayList<>();

    // === MONETIZATION ===
    private MonetizationManager monetization = new MonetizationManager();

    // === CONFIGURACIÓN ===
    private static final double PRESTIGE_REQUIREMENT = 1_000_000;
    private static final double PRESTIGE_MULTIPLIER = 0.05;
    private static final long EVENT_COOLDOWN = 180000; // 3 minutos entre eventos

    private Random random = new Random();

    private GameState() {
        initializeWorlds();
        initializeGenerators();
        initializeUpgrades();
        initializeAchievements();
        initializeMiniGames();
        initializePets();
        initializeDailyMissions();
        comboSystem = new ComboSystem();
        ownedWorkers = new ArrayList<>();
        contractTemplates = new ArrayList<>();
        lastUpdateTime = System.currentTimeMillis();
        lastSalaryPayTime = System.currentTimeMillis();
        lastEventTime = 0;
        lastMissionResetTime = System.currentTimeMillis();
        monetization.initFirstSession();
        recentlyUnlocked = new ArrayList<>();
    }

    public static GameState getInstance() {
        return instance;
    }

    // ==================== INICIALIZACIÓN ====================

    private void initializeWorlds() {
        worlds = new ArrayList<>();
        worlds.add(new World("world_1", World.WorldTheme.GARAGE, 0, 0));
        worlds.add(new World("world_2", World.WorldTheme.SILICON_VALLEY, 100000, 0));
        worlds.add(new World("world_3", World.WorldTheme.TOKYO_TECH, 1000000, 1));
        worlds.add(new World("world_4", World.WorldTheme.DUBAI_FUTURE, 10000000, 2));
        worlds.add(new World("world_5", World.WorldTheme.MARS_COLONY, 100000000, 3));
        worlds.add(new World("world_6", World.WorldTheme.QUANTUM_REALM, 1000000000, 5));
        worlds.add(new World("world_7", World.WorldTheme.CYBER_CITY, 10000000000L, 7));
        worlds.add(new World("world_8", World.WorldTheme.ATLANTIS_DEEP, 100000000000L, 10));
        worlds.add(new World("world_9", World.WorldTheme.VALHALLA_FORGE, 1000000000000L, 15));
        worlds.add(new World("world_10", World.WorldTheme.NEXUS_PRIME, 50000000000000L, 20));
    }

    private void initializeGenerators() {
        generators = new ArrayList<>();
        // === WORLD 0: Garage Startup ===
        generators.add(new Generator("coffee_stand", "☕ Coffee Stand", "Un pequeño puesto de café para programadores", "☕", 0.1, 15, 1.15, 0, 0));
        generators.add(new Generator("code_bootcamp", "💻 Code Bootcamp", "Entrena a la próxima generación de devs", "💻", 0.5, 100, 1.14, 50, 0));
        generators.add(new Generator("startup_garage", "🏠 Startup Garage", "Donde nacen las mejores ideas", "🏠", 2, 500, 1.13, 500, 0));
        generators.add(new Generator("app_studio", "📱 App Studio", "Desarrolla apps que conquistan el mercado", "📱", 6, 1800, 1.13, 2000, 0));

        // === WORLD 1: Silicon Valley ===
        generators.add(new Generator("server_farm", "🖥️ Server Farm", "Miles de servidores trabajando 24/7", "🖥️", 18, 5000, 1.12, 5000, 1));
        generators.add(new Generator("venture_fund", "💼 Venture Capital", "Fondo de inversión para startups prometedoras", "💼", 25, 8000, 1.12, 8000, 1));
        generators.add(new Generator("ai_lab", "🤖 AI Lab", "Investigación de inteligencia artificial de vanguardia", "🤖", 55, 15000, 1.11, 15000, 1));
        generators.add(new Generator("tech_incubator", "🏢 Tech Incubator", "Incubadora de empresas tecnológicas disruptivas", "🏢", 80, 25000, 1.11, 25000, 1));

        // === WORLD 2: Tokyo Tech ===
        generators.add(new Generator("cloud_network", "☁️ Cloud Network", "Infraestructura en la nube global con redundancia total", "☁️", 150, 45000, 1.11, 35000, 2));
        generators.add(new Generator("robot_factory", "🦾 Robot Factory", "Fábrica de robots autónomos de última generación", "🦾", 200, 70000, 1.10, 55000, 2));
        generators.add(new Generator("anime_studio", "🎌 Anime Studio", "Estudio de animación digital con IA integrada", "🎌", 280, 100000, 1.10, 80000, 2));
        generators.add(new Generator("crypto_mine", "⛏️ Crypto Mine", "Mina de criptomonedas a gran escala con energía verde", "⛏️", 400, 120000, 1.10, 100000, 2));

        // === WORLD 3: Dubai Future ===
        generators.add(new Generator("solar_tower", "🌞 Solar Tower", "Torre de energía solar concentrada en el desierto", "🌞", 600, 250000, 1.10, 200000, 3));
        generators.add(new Generator("luxury_mall", "🏬 Luxury Mall", "Centro comercial flotante con tiendas de lujo", "🏬", 800, 400000, 1.09, 350000, 3));
        generators.add(new Generator("biotech_lab", "🧬 Biotech Lab", "Laboratorio de modificación genética avanzada", "🧬", 1000, 550000, 1.09, 500000, 3));
        generators.add(new Generator("smart_city_hub", "🏙️ Smart City Hub", "Centro de control de ciudad inteligente automatizada", "🏙️", 1400, 800000, 1.09, 700000, 3));

        // === WORLD 4: Mars Colony ===
        generators.add(new Generator("oxygen_farm", "🌱 Oxygen Farm", "Granja de producción de oxígeno en Marte", "🌱", 2000, 1200000, 1.09, 1000000, 4));
        generators.add(new Generator("rover_fleet", "🚗 Rover Fleet", "Flota de rovers de exploración y minería marciana", "🚗", 2800, 1800000, 1.08, 1500000, 4));
        generators.add(new Generator("space_station", "🚀 Space Station", "Centro de datos y comunicaciones orbital", "🚀", 4000, 3000000, 1.08, 2500000, 4));
        generators.add(new Generator("colony_dome", "🔮 Colony Dome", "Domo habitacional autosuficiente con biodiversidad", "🔮", 6000, 5000000, 1.08, 4000000, 4));

        // === WORLD 5: Quantum Realm ===
        generators.add(new Generator("fusion_reactor", "⚡ Fusion Reactor", "Energía de fusión nuclear limpia e ilimitada", "⚡", 9000, 8000000, 1.08, 7000000, 5));
        generators.add(new Generator("quantum_computer", "⚛️ Quantum Computer", "El futuro de la computación cuántica masiva", "⚛️", 14000, 15000000, 1.07, 12000000, 5));
        generators.add(new Generator("dark_matter_lab", "🌑 Dark Matter Lab", "Extrae energía de la materia oscura del universo", "🌑", 22000, 30000000, 1.07, 25000000, 5));
        generators.add(new Generator("dimensional_gate", "🌀 Dimensional Gate", "Portal a dimensiones con recursos infinitos", "🌀", 35000, 60000000, 1.07, 50000000, 5));

        // === WORLD 6: Cyber City ===
        generators.add(new Generator("nanobot_swarm", "🤏 Nanobot Swarm", "Ejércitos de nanobots autoreplicantes inteligentes", "🤏", 55000, 120000000, 1.07, 100000000, 6));
        generators.add(new Generator("neural_network", "🧠 Neural Network", "Red neuronal consciente que genera soluciones", "🧠", 85000, 250000000, 1.06, 200000000, 6));
        generators.add(new Generator("hologram_arcade", "🎮 Hologram Arcade", "Entretenimiento holográfico inmersivo multidimensional", "🎮", 130000, 500000000, 1.06, 400000000, 6));
        generators.add(new Generator("cyber_bank", "🏦 Cyber Bank", "Banco cuántico con criptomonedas del futuro", "🏦", 200000, 1000000000.0, 1.06, 800000000, 6));

        // === WORLD 7: Atlantis Deep ===
        generators.add(new Generator("coral_refinery", "🐚 Coral Refinery", "Refinería de minerales marinos del fondo abisal", "🐚", 320000, 2500000000.0, 1.06, 2000000000L, 7));
        generators.add(new Generator("leviathan_farm", "🐋 Leviathan Farm", "Cría de criaturas abisales para energía biológica", "🐋", 500000, 6000000000.0, 1.05, 5000000000L, 7));
        generators.add(new Generator("trident_forge", "🔱 Trident Forge", "Forja submarina de artefactos de poder místico", "🔱", 800000, 15000000000.0, 1.05, 12000000000L, 7));
        generators.add(new Generator("abyssal_core", "🌊 Abyssal Core", "Núcleo energético del fondo del océano infinito", "🌊", 1300000, 40000000000.0, 1.05, 30000000000L, 7));

        // === WORLD 8: Valhalla Forge ===
        generators.add(new Generator("rune_workshop", "⚒️ Rune Workshop", "Taller de runas nórdicas de poder ancestral", "⚒️", 2100000, 100000000000.0, 1.05, 80000000000L, 8));
        generators.add(new Generator("mjolnir_forge", "🔨 Mjolnir Forge", "La forja del martillo de los dioses del trueno", "🔨", 3500000, 300000000000.0, 1.04, 250000000000L, 8));
        generators.add(new Generator("yggdrasil_root", "🌳 Yggdrasil Root", "Raíz del árbol del mundo que conecta los reinos", "🌳", 6000000, 800000000000.0, 1.04, 600000000000L, 8));
        generators.add(new Generator("odin_throne", "👁️ Odin's Throne", "El trono de la sabiduría infinita de los nueve reinos", "👁️", 10000000, 2000000000000.0, 1.04, 1500000000000L, 8));

        // === WORLD 9: Nexus Prime ===
        generators.add(new Generator("multiverse_hub", "🌌 Multiverse Hub", "Conecta con realidades paralelas e infinitas", "🌌", 18000000, 6000000000000.0, 1.04, 5000000000000L, 9));
        generators.add(new Generator("reality_engine", "🎭 Reality Engine", "Crea y destruye realidades a voluntad absoluta", "🎭", 30000000, 20000000000000.0, 1.03, 15000000000000L, 9));
        generators.add(new Generator("cosmic_forge", "✨ Cosmic Forge", "Forja estrellas, galaxias y universos enteros", "✨", 50000000, 60000000000000.0, 1.03, 50000000000000L, 9));
        generators.add(new Generator("omniscience_core", "🔮 Omniscience Core", "Sabiduría infinita del multiverso unificado", "🔮", 100000000, 200000000000000.0, 1.03, 150000000000000L, 9));
    }

    private void initializeUpgrades() {
        upgrades = new ArrayList<>();
        // Tap upgrades
        upgrades.add(new Upgrade("tap_power_1", "💪 Tap Power I", "+5 coins por tap", "💪", Upgrade.UpgradeType.TAP_POWER, 100, 2.0, 5, 15));
        upgrades.add(new Upgrade("tap_power_2", "💪 Tap Power II", "+25 coins por tap", "💪", Upgrade.UpgradeType.TAP_POWER, 5000, 2.2, 25, 10));
        upgrades.add(new Upgrade("tap_power_3", "💪 Tap Power III", "+200 coins por tap", "💪", Upgrade.UpgradeType.TAP_POWER, 200000, 2.5, 200, 8));
        upgrades.add(new Upgrade("tap_mult_1", "⚡ Tap Multiplier", "x1.5 coins por tap", "⚡", Upgrade.UpgradeType.TAP_MULTIPLIER, 500, 3.0, 0.5, 8));
        // Critical upgrades
        upgrades.add(new Upgrade("crit_chance", "🎯 Critical Chance", "+2% probabilidad de crítico", "🎯", Upgrade.UpgradeType.CRITICAL_CHANCE, 1000, 2.5, 0.02, 15));
        upgrades.add(new Upgrade("crit_mult", "💥 Critical Power", "+0.5x multiplicador crítico", "💥", Upgrade.UpgradeType.CRITICAL_MULTIPLIER, 2000, 2.5, 0.5, 12));
        // Production upgrades
        upgrades.add(new Upgrade("global_prod", "🌍 Global Production", "+10% producción global", "🌍", Upgrade.UpgradeType.GLOBAL_PRODUCTION, 5000, 3.0, 0.10, 25));
        upgrades.add(new Upgrade("global_prod_2", "🏭 Industrial Boost", "+20% producción global", "🏭", Upgrade.UpgradeType.GLOBAL_PRODUCTION, 500000, 3.5, 0.20, 15));
        upgrades.add(new Upgrade("global_prod_3", "🚀 Hyper Production", "+50% producción global", "🚀", Upgrade.UpgradeType.GLOBAL_PRODUCTION, 50000000, 4.0, 0.50, 10));
        // Offline
        upgrades.add(new Upgrade("offline_bonus", "😴 Offline Earnings", "+10% ganancias offline", "😴", Upgrade.UpgradeType.OFFLINE_BONUS, 10000, 3.0, 0.10, 5));
        // Special late-game
        upgrades.add(new Upgrade("tap_mult_2", "🌟 Star Power", "x2 coins por tap", "🌟", Upgrade.UpgradeType.TAP_MULTIPLIER, 1000000, 4.0, 1.0, 5));
        upgrades.add(new Upgrade("crit_chance_2", "⚡ Lightning Reflexes", "+3% probabilidad de crítico", "⚡", Upgrade.UpgradeType.CRITICAL_CHANCE, 5000000, 3.0, 0.03, 10));
    }

    private void initializeAchievements() {
        achievements = new ArrayList<>();
        // Tap milestones
        achievements.add(new Achievement("taps_100", "👆 First Steps", "Haz 100 taps", "👆", Achievement.AchievementType.TOTAL_TAPS, 100, 50));
        achievements.add(new Achievement("taps_1000", "🖐️ Tap Enthusiast", "Haz 1,000 taps", "🖐️", Achievement.AchievementType.TOTAL_TAPS, 1000, 200));
        achievements.add(new Achievement("taps_10000", "👊 Tap Master", "Haz 10,000 taps", "👊", Achievement.AchievementType.TOTAL_TAPS, 10000, 1000));
        achievements.add(new Achievement("taps_100000", "🏆 Tap Legend", "Haz 100,000 taps", "🏆", Achievement.AchievementType.TOTAL_TAPS, 100000, 10000));
        achievements.add(new Achievement("taps_500000", "⚡ Tap God", "Haz 500,000 taps", "⚡", Achievement.AchievementType.TOTAL_TAPS, 500000, 100000));
        achievements.add(new Achievement("taps_1m", "🌟 Tap Overlord", "Haz 1,000,000 taps", "🌟", Achievement.AchievementType.TOTAL_TAPS, 1000000, 1000000));
        // Coin milestones
        achievements.add(new Achievement("coins_1000", "💰 Pocket Money", "Gana 1,000 coins totales", "💰", Achievement.AchievementType.TOTAL_COINS_EARNED, 1000, 100));
        achievements.add(new Achievement("coins_100000", "💎 Getting Rich", "Gana 100,000 coins totales", "💎", Achievement.AchievementType.TOTAL_COINS_EARNED, 100000, 5000));
        achievements.add(new Achievement("coins_1m", "👑 Millionaire", "Gana 1,000,000 coins totales", "👑", Achievement.AchievementType.TOTAL_COINS_EARNED, 1000000, 50000));
        achievements.add(new Achievement("coins_1b", "🌟 Billionaire", "Gana 1,000,000,000 coins totales", "🌟", Achievement.AchievementType.TOTAL_COINS_EARNED, 1000000000, 1000000));
        achievements.add(new Achievement("coins_1t", "🔱 Trillionaire", "Gana 1,000,000,000,000 coins", "🔱", Achievement.AchievementType.TOTAL_COINS_EARNED, 1000000000000L, 100000000));
        achievements.add(new Achievement("coins_1qa", "👁️ Quadrillionaire", "Gana 1Qa coins", "👁️", Achievement.AchievementType.TOTAL_COINS_EARNED, 1000000000000000L, 10000000000L));
        // Production milestones
        achievements.add(new Achievement("prod_100", "⚙️ Automation", "Produce 100/seg", "⚙️", Achievement.AchievementType.PRODUCTION_PER_SECOND, 100, 500));
        achievements.add(new Achievement("prod_10000", "🏭 Industrial", "Produce 10,000/seg", "🏭", Achievement.AchievementType.PRODUCTION_PER_SECOND, 10000, 10000));
        achievements.add(new Achievement("prod_1m", "🚀 Mass Production", "Produce 1,000,000/seg", "🚀", Achievement.AchievementType.PRODUCTION_PER_SECOND, 1000000, 500000));
        achievements.add(new Achievement("prod_1b", "⚛️ Infinite Output", "Produce 1,000,000,000/seg", "⚛️", Achievement.AchievementType.PRODUCTION_PER_SECOND, 1000000000, 50000000));
        // Prestige milestones
        achievements.add(new Achievement("prestige_1", "⭐ Reborn", "Alcanza Prestigio 1", "⭐", Achievement.AchievementType.PRESTIGE_LEVEL, 1, 25000));
        achievements.add(new Achievement("prestige_3", "🌙 Transcendent", "Alcanza Prestigio 3", "🌙", Achievement.AchievementType.PRESTIGE_LEVEL, 3, 250000));
        achievements.add(new Achievement("prestige_5", "☀️ Ascended", "Alcanza Prestigio 5", "☀️", Achievement.AchievementType.PRESTIGE_LEVEL, 5, 2500000));
        achievements.add(new Achievement("prestige_10", "🌌 Cosmic Being", "Alcanza Prestigio 10", "🌌", Achievement.AchievementType.PRESTIGE_LEVEL, 10, 50000000));
        // Special
        achievements.add(new Achievement("combo_25", "🔥 Combo Master", "Alcanza un combo de 25x", "🔥", Achievement.AchievementType.MAX_COMBO, 25, 5000));
        achievements.add(new Achievement("worlds_3", "🌍 World Traveler", "Desbloquea 3 mundos", "🌍", Achievement.AchievementType.WORLDS_UNLOCKED, 3, 25000));
        achievements.add(new Achievement("worlds_6", "🌠 Galaxy Explorer", "Desbloquea 6 mundos", "🌠", Achievement.AchievementType.WORLDS_UNLOCKED, 6, 500000));
        achievements.add(new Achievement("worlds_10", "🌌 Universe Conqueror", "Desbloquea todos los mundos", "🌌", Achievement.AchievementType.WORLDS_UNLOCKED, 10, 50000000));

        // === WORKER ACHIEVEMENTS ===
        achievements.add(new Achievement("workers_1", "👷 First Hire", "Contrata tu primer trabajador", "👷", Achievement.AchievementType.WORKERS_HIRED, 1, 5000));
        achievements.add(new Achievement("workers_5", "🏗️ Small Team", "Contrata 5 trabajadores", "🏗️", Achievement.AchievementType.WORKERS_HIRED, 5, 25000));
        achievements.add(new Achievement("workers_15", "🏢 Growing Company", "Contrata 15 trabajadores", "🏢", Achievement.AchievementType.WORKERS_HIRED, 15, 100000));
        achievements.add(new Achievement("workers_30", "🏭 Corporation", "Contrata 30 trabajadores", "🏭", Achievement.AchievementType.WORKERS_HIRED, 30, 500000));
        achievements.add(new Achievement("workers_50", "🌐 Enterprise", "Contrata 50 trabajadores", "🌐", Achievement.AchievementType.WORKERS_HIRED, 50, 5000000));
        achievements.add(new Achievement("workers_100", "👑 Mega Corp CEO", "Contrata 100 trabajadores", "👑", Achievement.AchievementType.WORKERS_HIRED, 100, 50000000));

        // === STRIKE RESOLUTION ===
        achievements.add(new Achievement("strike_1", "🤝 Negotiator", "Resuelve tu primera huelga", "🤝", Achievement.AchievementType.WORKERS_ON_STRIKE_RESOLVED, 1, 10000));
        achievements.add(new Achievement("strike_5", "⚖️ Fair Boss", "Resuelve 5 huelgas", "⚖️", Achievement.AchievementType.WORKERS_ON_STRIKE_RESOLVED, 5, 50000));
        achievements.add(new Achievement("strike_20", "🕊️ Peace Broker", "Resuelve 20 huelgas", "🕊️", Achievement.AchievementType.WORKERS_ON_STRIKE_RESOLVED, 20, 500000));
        achievements.add(new Achievement("strike_50", "🏛️ Labor Legend", "Resuelve 50 huelgas", "🏛️", Achievement.AchievementType.WORKERS_ON_STRIKE_RESOLVED, 50, 5000000));

        // === CONTRACT ACHIEVEMENTS ===
        achievements.add(new Achievement("contracts_1", "📋 First Contract", "Crea tu primer contrato", "📋", Achievement.AchievementType.CONTRACTS_CREATED, 1, 5000));
        achievements.add(new Achievement("contracts_5", "📑 Contract Writer", "Crea 5 contratos", "📑", Achievement.AchievementType.CONTRACTS_CREATED, 5, 25000));
        achievements.add(new Achievement("contracts_15", "📜 Legal Expert", "Crea 15 contratos diferentes", "📜", Achievement.AchievementType.CONTRACTS_CREATED, 15, 200000));

        // === PET ACHIEVEMENTS ===
        achievements.add(new Achievement("pets_1", "🐾 Pet Parent", "Compra tu primera mascota", "🐾", Achievement.AchievementType.PETS_OWNED, 1, 2000));
        achievements.add(new Achievement("pets_3", "🏠 Pet Family", "Ten 3 mascotas", "🏠", Achievement.AchievementType.PETS_OWNED, 3, 15000));
        achievements.add(new Achievement("pets_5", "🐾 Pet Collector", "Ten 5 mascotas", "🐾", Achievement.AchievementType.PETS_OWNED, 5, 50000));
        achievements.add(new Achievement("pets_10", "🏰 Pet Haven", "Ten 10 mascotas", "🏰", Achievement.AchievementType.PETS_OWNED, 10, 500000));
        achievements.add(new Achievement("pets_max", "👑 Pet Emperor", "Ten el máximo de mascotas (15)", "👑", Achievement.AchievementType.PETS_OWNED, 15, 5000000));

        // === BREEDING ACHIEVEMENTS ===
        achievements.add(new Achievement("breed_1", "🧬 First Offspring", "Cría tu primera mascota", "🧬", Achievement.AchievementType.PETS_BRED, 1, 10000));
        achievements.add(new Achievement("breed_5", "🔬 Genetics Novice", "Cría 5 mascotas", "🔬", Achievement.AchievementType.PETS_BRED, 5, 50000));
        achievements.add(new Achievement("breed_15", "🧪 Master Breeder", "Cría 15 mascotas", "🧪", Achievement.AchievementType.PETS_BRED, 15, 500000));
        achievements.add(new Achievement("breed_30", "🏆 Legendary Breeder", "Cría 30 mascotas", "🏆", Achievement.AchievementType.PETS_BRED, 30, 5000000));
        achievements.add(new Achievement("breed_50", "⭐ Genetic Overlord", "Cría 50 mascotas", "⭐", Achievement.AchievementType.PETS_BRED, 50, 50000000));

        // === MUTATION ACHIEVEMENTS ===
        achievements.add(new Achievement("mutant_1", "✨ First Mutation!", "Consigue tu primera mutación rara", "✨", Achievement.AchievementType.RARE_MUTATIONS, 1, 100000));
        achievements.add(new Achievement("mutant_3", "🌟 Mutation Streak", "Consigue 3 mutaciones raras", "🌟", Achievement.AchievementType.RARE_MUTATIONS, 3, 500000));
        achievements.add(new Achievement("mutant_5", "💫 Evolutionary Leap", "Consigue 5 mutaciones raras", "💫", Achievement.AchievementType.RARE_MUTATIONS, 5, 2500000));
        achievements.add(new Achievement("mutant_10", "🔮 Mutation Master", "Consigue 10 mutaciones raras", "🔮", Achievement.AchievementType.RARE_MUTATIONS, 10, 25000000));

        // === HYBRID GENERATION ===
        achievements.add(new Achievement("gen_2", "🧬 Gen 2 Hybrid", "Cría un híbrido de generación 2", "🧬", Achievement.AchievementType.HYBRID_GENERATION, 2, 25000));
        achievements.add(new Achievement("gen_3", "🔬 Gen 3 Lineage", "Cría un híbrido de generación 3", "🔬", Achievement.AchievementType.HYBRID_GENERATION, 3, 100000));
        achievements.add(new Achievement("gen_5", "🧪 Gen 5 Dynasty", "Cría un híbrido de generación 5", "🧪", Achievement.AchievementType.HYBRID_GENERATION, 5, 1000000));
        achievements.add(new Achievement("gen_10", "👑 Gen 10 Apex", "Cría un híbrido de generación 10", "👑", Achievement.AchievementType.HYBRID_GENERATION, 10, 50000000));

        // === NURSERY WELFARE ===
        achievements.add(new Achievement("welfare_90", "💚 Happy Nursery", "Alcanza 90% de bienestar", "💚", Achievement.AchievementType.NURSERY_WELFARE, 90, 50000));
        achievements.add(new Achievement("welfare_100", "🏆 Perfect Paradise", "Alcanza 100% de bienestar", "🏆", Achievement.AchievementType.NURSERY_WELFARE, 100, 500000));

        // === TRAIT COLLECTION ===
        achievements.add(new Achievement("traits_5", "🔍 Trait Seeker", "Descubre 5 rasgos diferentes", "🔍", Achievement.AchievementType.TRAITS_DISCOVERED, 5, 25000));
        achievements.add(new Achievement("traits_10", "📚 Trait Scholar", "Descubre 10 rasgos diferentes", "📚", Achievement.AchievementType.TRAITS_DISCOVERED, 10, 250000));
        achievements.add(new Achievement("traits_all", "🌟 Trait Completionist", "Descubre todos los rasgos (15)", "🌟", Achievement.AchievementType.TRAITS_DISCOVERED, 15, 10000000));
    }

    private void initializeMiniGames() {
        miniGames = new ArrayList<>();
        miniGames.add(new MiniGame(MiniGame.MiniGameType.FORTUNE_WHEEL, 3, 1000));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.TAP_FRENZY, 5, 500));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.LUCKY_BOX, 3, 800));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.COIN_RAIN, 4, 600));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.MEMORY_MATCH, 3, 1200));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.BOSS_BATTLE, 2, 2000));
        // Playable mini-games (higher rewards, fewer plays)
        miniGames.add(new MiniGame(MiniGame.MiniGameType.COSMIC_BILLIARDS, 3, 3000));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.ASTEROID_DODGE, 4, 2500));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.STAR_CATCHER, 5, 1800));
        miniGames.add(new MiniGame(MiniGame.MiniGameType.GRAVITY_SLINGSHOT, 2, 4000));
    }

    private void initializePets() {
        pets = new ArrayList<>();
        for (Pet.PetType type : Pet.PetType.values()) {
            pets.add(new Pet(type));
        }
    }

    private void initializeDailyMissions() {
        dailyMissions = DailyMission.generateDailyMissions(getProductionPerSecond(), prestigeLevel);
    }

    // ==================== ACCIONES DE JUEGO ====================

    /**
     * Ejecuta un tap con sistema de combo y eventos
     */
    public TapResult tap() {
        totalTaps++;
        globalTotalTaps++;

        // Sistema de combo
        double comboMultiplier = comboSystem.registerTap();

        double tapValue = calculateTapValue();
        boolean isCritical = random.nextDouble() < getEffectiveCriticalChance();

        if (isCritical) {
            tapValue *= criticalMultiplier;
            totalCriticalHits++;
            globalTotalCriticalHits++;
            updateMissionProgress(DailyMission.MissionType.CRITICAL_HITS, 1);
        }

        // Aplicar combo
        tapValue *= comboMultiplier;

        // Aplicar multiplicador de mundo
        tapValue *= getCurrentWorld().getProductionMultiplier();

        // Aplicar prestigio
        tapValue *= (1 + prestigeLevel * PRESTIGE_MULTIPLIER);

        // Aplicar bonus de mascota activa
        if (activePet != null && activePet.isOwned()) {
            if (activePet.getType().bonusType == Pet.BonusType.TAP_POWER) {
                tapValue *= (1 + activePet.getCurrentBonus());
            }
        }

        // Aplicar evento activo
        if (activeEvent != null && activeEvent.isActive()) {
            tapValue *= activeEvent.getType().productionMultiplier;
            activeEvent.registerTap(tapValue);
        }

        coins += tapValue;
        totalCoinsEarned += tapValue;
        globalTotalCoinsEarned += tapValue;
        getCurrentWorld().addEarnings(tapValue);

        // Actualizar misiones
        updateMissionProgress(DailyMission.MissionType.TAP_COUNT, 1);
        updateMissionProgress(DailyMission.MissionType.EARN_COINS, tapValue);

        // Verificar combo para misión
        if (comboSystem.getCurrentCombo() > 0) {
            updateMissionProgress(DailyMission.MissionType.COMBO_STREAK, comboSystem.getCurrentCombo());
            updateMissionProgress(DailyMission.MissionType.REACH_COMBO_MULTI, comboSystem.getMultiplier());
            // Track max combo for achievements
            if (comboSystem.getCurrentCombo() > maxComboReached) {
                maxComboReached = comboSystem.getCurrentCombo();
            }
        }

        checkAchievements();
        checkGeneratorUnlocks();
        checkWorldUnlocks();
        trySpawnEvent();

        return new TapResult(tapValue, isCritical, comboSystem.getCurrentCombo(), comboMultiplier);
    }

    private double getEffectiveCriticalChance() {
        double chance = criticalChance;

        // Bonus de evento
        if (activeEvent != null && activeEvent.isActive()) {
            chance += activeEvent.getType().criticalBonus;
        }

        // Bonus de mascota
        if (activePet != null && activePet.isOwned() &&
            activePet.getType().bonusType == Pet.BonusType.CRIT_CHANCE) {
            chance += activePet.getCurrentBonus();
        }

        return Math.min(chance, 0.75); // Max 75%
    }

    private double calculateTapValue() {
        double base = basePerTap;
        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.TAP_POWER && u.getCurrentLevel() > 0) {
                base += u.getTotalEffect();
            }
        }
        double mult = tapMultiplier;
        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.TAP_MULTIPLIER && u.getCurrentLevel() > 0) {
                mult += u.getTotalEffect();
            }
        }
        return base * mult;
    }

    public double updateProduction(long currentTime) {
        long deltaTime = currentTime - lastUpdateTime;
        double seconds = deltaTime / 1000.0;

        if (seconds > 0) {
            // Process salaries every SALARY_INTERVAL_MS
            processSalaries(currentTime);
            // Update generator worker productivity from assigned workers
            updateWorkerProductivity();

            double production = getProductionPerSecond() * seconds;
            coins += production;
            totalCoinsEarned += production;
            globalTotalCoinsEarned += production;
            lastUpdateTime = currentTime;

            checkAchievements();
            checkGeneratorUnlocks();
            checkWorldUnlocks();
            checkDailyMissionReset();
            updatePets();

            return production;
        }
        return 0;
    }

    public double getProductionPerSecond() {
        double total = 0;
        for (Generator gen : generators) {
            if (gen.isUnlocked()) {
                total += gen.getProductionPerSecond();
            }
        }

        total *= productionMultiplier;
        total *= getCurrentWorld().getProductionMultiplier();

        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.GLOBAL_PRODUCTION && u.getCurrentLevel() > 0) {
                total *= (1 + u.getTotalEffect());
            }
        }

        total *= (1 + prestigeLevel * PRESTIGE_MULTIPLIER);

        // Bonus de mascota
        if (activePet != null && activePet.isOwned()) {
            if (activePet.getType().bonusType == Pet.BonusType.PRODUCTION ||
                activePet.getType().bonusType == Pet.BonusType.GLOBAL_MULT ||
                activePet.getType().bonusType == Pet.BonusType.MEGA_MULT) {
                total *= (1 + activePet.getCurrentBonus());
            }
        }

        // Evento activo (legacy)
        if (activeEvent != null && activeEvent.isActive()) {
            total *= activeEvent.getType().productionMultiplier;
        }

        // Event system multiplier
        double eventMult = getEventProductionMultiplier();
        if (eventMult != 0) total *= (1.0 + eventMult);

        // Monetization multiplier (VIP + boosts + whale)
        total *= monetization.getTotalProductionMultiplier();

        return total;
    }

    // ==================== SISTEMA DE MUNDOS ====================

    public World getCurrentWorld() {
        return worlds.get(currentWorldIndex);
    }

    public boolean switchWorld(int index) {
        if (index >= 0 && index < worlds.size() && worlds.get(index).isUnlocked()) {
            currentWorldIndex = index;
            return true;
        }
        return false;
    }

    public boolean buyWorld(int index) {
        if (index < 0 || index >= worlds.size()) return false;
        World world = worlds.get(index);
        if (world.canUnlock(coins, prestigeLevel)) {
            coins -= world.getUnlockCost();
            world.setUnlocked(true);
            return true;
        }
        return false;
    }

    private void checkWorldUnlocks() {
        for (World world : worlds) {
            world.tryUnlock(totalCoinsEarned, prestigeLevel);
        }
    }

    // ==================== SISTEMA DE EVENTOS ====================

    private void trySpawnEvent() {
        if (activeEvent != null && activeEvent.isActive()) return;

        long now = System.currentTimeMillis();
        if (now - lastEventTime < EVENT_COOLDOWN) return;

        // 5% de probabilidad por tap de generar evento
        if (random.nextDouble() < 0.05) {
            activeEvent = SpecialEvent.generateRandomEvent();
            lastEventTime = now;
        }
    }

    public SpecialEvent getActiveEvent() {
        if (activeEvent != null && !activeEvent.isActive()) {
            // Evento terminado, dar recompensa
            double reward = activeEvent.calculateReward(getProductionPerSecond());
            coins += reward;
            totalCoinsEarned += reward;
            totalEventsCompleted++;
            activeEvent = null;
        }
        return activeEvent;
    }

    // ==================== SISTEMA DE MISIONES ====================

    public void updateMissionProgress(DailyMission.MissionType type, double amount) {
        for (DailyMission mission : dailyMissions) {
            if (mission.getType() == type && !mission.isClaimed()) {
                if (type == DailyMission.MissionType.COMBO_STREAK ||
                    type == DailyMission.MissionType.REACH_PRODUCTION ||
                    type == DailyMission.MissionType.REACH_COMBO_MULTI ||
                    type == DailyMission.MissionType.REACH_TAP_TOTAL) {
                    mission.setProgress(amount);
                } else {
                    mission.addProgress(amount);
                }
            }
        }
    }

    public boolean claimMissionReward(int index) {
        if (index < 0 || index >= dailyMissions.size()) return false;
        DailyMission mission = dailyMissions.get(index);
        if (mission.claim()) {
            coins += mission.getCoinReward();
            totalCoinsEarned += mission.getCoinReward();
            return true;
        }
        return false;
    }

    private void checkDailyMissionReset() {
        long now = System.currentTimeMillis();
        long dayInMs = 24 * 60 * 60 * 1000;
        if (now - lastMissionResetTime > dayInMs) {
            dailyMissions = DailyMission.generateDailyMissions(getProductionPerSecond(), prestigeLevel);
            lastMissionResetTime = now;
            for (MiniGame game : miniGames) {
                game.resetDaily();
            }
        }
    }

    // ==================== SISTEMA DE MASCOTAS ====================

    public boolean buyPet(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned() && coins >= pet.getPurchaseCost()) {
            double cost = pet.getPurchaseCost();
            coins -= cost;
            pet.setOwned(true);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            if (activePet == null) {
                activePet = pet;
            }
            return true;
        }
        return false;
    }

    public void setActivePet(int index) {
        if (index >= 0 && index < pets.size() && pets.get(index).isOwned()) {
            activePet = pets.get(index);
        }
    }

    public boolean feedPet() {
        if (activePet == null || !activePet.isOwned()) return false;
        double cost = activePet.getFeedCost(getProductionPerSecond());
        if (activePet.feed(cost, coins)) {
            coins -= cost;
            updateMissionProgress(DailyMission.MissionType.FEED_PET, 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            return true;
        }
        return false;
    }

    public boolean petPet() {
        if (activePet == null || !activePet.isOwned()) return false;
        if (activePet.pet()) {
            updateMissionProgress(DailyMission.MissionType.PET_YOUR_PET, 1);
            return true;
        }
        return false;
    }

    // --- Indexed pet care (for Nursery) ---

    public boolean feedPetAt(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned() || !pet.isAlive()) return false;
        double cost = pet.getFeedCost(getProductionPerSecond());
        if (pet.feed(cost, coins)) {
            coins -= cost;
            updateMissionProgress(DailyMission.MissionType.FEED_PET, 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            return true;
        }
        return false;
    }

    public boolean petPetAt(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned()) return false;
        if (pet.pet()) {
            updateMissionProgress(DailyMission.MissionType.PET_YOUR_PET, 1);
            return true;
        }
        return false;
    }

    public boolean cleanPet(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned() || !pet.isAlive()) return false;
        double cost = pet.getCleanCost(getProductionPerSecond());
        if (pet.clean(cost, coins)) { coins -= cost; return true; }
        return false;
    }

    public boolean playPet(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned()) return false;
        return pet.play();
    }

    public boolean restPet(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned()) return false;
        return pet.rest();
    }

    public boolean treatPetDisease(int index, Pet.Disease disease) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned() || !pet.isAlive()) return false;
        if (pet.treatDisease(disease, coins)) { coins -= disease.treatmentCost; return true; }
        return false;
    }

    public boolean revivePet(int index) {
        if (index < 0 || index >= pets.size()) return false;
        Pet pet = pets.get(index);
        if (!pet.isOwned() || pet.isAlive()) return false;
        double cost = pet.getReviveCost();
        if (pet.revive(cost, coins)) { coins -= cost; return true; }
        return false;
    }

    private void updatePets() {
        for (Pet pet : pets) {
            if (pet.isOwned()) {
                pet.updateMood();
            }
        }
    }

    // ==================== COMPRAS ====================

    // === BUSINESS PROPERTY UPGRADES (via Business Map) ===

    public boolean upgradeGeneratorWorkers(int index) {
        if (index < 0 || index >= generators.size()) return false;
        Generator gen = generators.get(index);
        if (!gen.isUnlocked() || gen.getOwned() == 0) return false;
        double cost = gen.getWorkerUpgradeCost();
        if (coins >= cost) {
            coins -= cost;
            gen.setWorkers(gen.getWorkers() + 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            return true;
        }
        return false;
    }

    public boolean upgradeGeneratorSize(int index) {
        if (index < 0 || index >= generators.size()) return false;
        Generator gen = generators.get(index);
        if (!gen.isUnlocked() || gen.getOwned() == 0) return false;
        double cost = gen.getSizeUpgradeCost();
        if (coins >= cost) {
            coins -= cost;
            gen.setLocalSizeM2(gen.getLocalSizeM2() + 10);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            return true;
        }
        return false;
    }

    public boolean upgradeGeneratorLocation(int index) {
        if (index < 0 || index >= generators.size()) return false;
        Generator gen = generators.get(index);
        if (!gen.isUnlocked() || gen.getOwned() == 0 || !gen.canUpgradeLocation()) return false;
        double cost = gen.getLocationUpgradeCost();
        if (coins >= cost) {
            coins -= cost;
            gen.setLocationTier(gen.getLocationTier() + 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            return true;
        }
        return false;
    }

    public boolean buyGenerator(int index) {
        if (index < 0 || index >= generators.size()) return false;
        Generator gen = generators.get(index);
        double cost = gen.getCurrentCost();
        if (coins >= cost && gen.isUnlocked()) {
            coins -= cost;
            gen.setOwned(gen.getOwned() + 1);
            totalGeneratorsBought++;
            globalTotalGeneratorsBought++;
            updateMissionProgress(DailyMission.MissionType.BUY_GENERATORS, 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            checkAchievements();
            return true;
        }
        return false;
    }

    public boolean buyUpgrade(int index) {
        if (index < 0 || index >= upgrades.size()) return false;
        Upgrade upgrade = upgrades.get(index);
        double cost = upgrade.getCurrentCost();
        if (coins >= cost && upgrade.canUpgrade()) {
            coins -= cost;
            upgrade.setCurrentLevel(upgrade.getCurrentLevel() + 1);
            updateMissionProgress(DailyMission.MissionType.BUY_UPGRADES, 1);
            updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
            updateMultipliers();
            checkAchievements();
            return true;
        }
        return false;
    }

    private void updateMultipliers() {
        criticalChance = 0.05;
        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.CRITICAL_CHANCE) {
                criticalChance += u.getTotalEffect();
            }
        }
        criticalChance = Math.min(criticalChance, 0.5);

        criticalMultiplier = 2.0;
        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.CRITICAL_MULTIPLIER) {
                criticalMultiplier += u.getTotalEffect();
            }
        }

        offlineMultiplier = 0.5;
        for (Upgrade u : upgrades) {
            if (u.getType() == Upgrade.UpgradeType.OFFLINE_BONUS) {
                offlineMultiplier += u.getTotalEffect();
            }
        }
        offlineMultiplier = Math.min(offlineMultiplier, 1.0);
    }

    // ==================== PRESTIGIO ====================

    public boolean canPrestige() {
        return totalCoinsEarned >= PRESTIGE_REQUIREMENT * Math.pow(10, prestigeLevel);
    }

    public double getPrestigePointsAvailable() {
        if (!canPrestige()) return 0;
        return Math.floor(Math.sqrt(totalCoinsEarned / PRESTIGE_REQUIREMENT));
    }

    public void performPrestige() {
        if (!canPrestige()) return;

        double newPoints = getPrestigePointsAvailable();
        prestigePoints += newPoints;
        prestigeLevel++;
        totalPrestigesPerformed++;

        coins = 0;
        totalCoinsEarned = 0;
        totalTaps = 0;
        totalCriticalHits = 0;
        totalGeneratorsBought = 0;
        basePerTap = 1;
        tapMultiplier = 1.0;
        productionMultiplier = 1.0;

        for (Generator gen : generators) {
            gen.setOwned(0);
            gen.setLevel(1);
            gen.setProductionMultiplier(1.0);
            gen.setUnlocked(gen.getUnlockRequirement() == 0);
            gen.setWorkers(0);
            gen.setLocalSizeM2(10);
            gen.setLocationTier(0);
            gen.setBranches(1);
            gen.setAssignedWorkerProductivity(0);
        }

        // Unassign all workers but keep them (they are valuable gacha items)
        for (Worker w : ownedWorkers) {
            w.setAssignedGeneratorId(null);
        }

        for (Upgrade up : upgrades) {
            up.setCurrentLevel(0);
        }

        currentWorldIndex = 0;
        for (int i = 1; i < worlds.size(); i++) {
            worlds.get(i).setUnlocked(false);
        }

        updateMultipliers();
    }

    public double getPrestigeMultiplier() {
        return 1 + prestigeLevel * PRESTIGE_MULTIPLIER;
    }

    // ==================== LOGROS ====================

    public void checkAchievements() {
        double totalReward = 0;
        for (Achievement achievement : achievements) {
            if (!achievement.isUnlocked()) {
                double currentValue = getAchievementProgress(achievement);
                if (achievement.checkProgress(currentValue)) {
                    recentlyUnlocked.add(achievement);
                    totalReward += achievement.getReward();
                }
            }
        }
        if (totalReward > 0) {
            coins += totalReward;
            totalCoinsEarned += totalReward;
            globalTotalCoinsEarned += totalReward;
        }
    }

    private double getAchievementProgress(Achievement achievement) {
        switch (achievement.getType()) {
            case TOTAL_TAPS: return totalTaps;
            case TOTAL_COINS_EARNED: return totalCoinsEarned;
            case COINS_OWNED: return coins;
            case PRODUCTION_PER_SECOND: return getProductionPerSecond();
            case PRESTIGE_LEVEL: return prestigeLevel;
            case WORKERS_HIRED: return ownedWorkers.size();
            case WORKERS_ON_STRIKE_RESOLVED: return totalStrikesResolved;
            case PETS_OWNED: {
                int c = 0; for (Pet p : pets) { if (p.isOwned() && !p.isIncubating()) c++; } return c;
            }
            case PETS_BRED: return totalPetsBred;
            case RARE_MUTATIONS: return totalRareMutations;
            case NURSERY_WELFARE: return nurseryWelfareLevel;
            case CONTRACTS_CREATED: return contractTemplates.size();
            case HYBRID_GENERATION: {
                int maxGen = 0; for (Pet p : pets) { if (p.isOwned()) maxGen = Math.max(maxGen, p.getGeneration()); } return maxGen;
            }
            case TRAITS_DISCOVERED: {
                java.util.Set<Pet.PetTrait> disc = new java.util.HashSet<>();
                for (Pet p : pets) { if (p.isOwned()) disc.addAll(p.getTraits()); }
                return disc.size();
            }
            case WORLDS_UNLOCKED: {
                int c = 0; for (World w : worlds) { if (w.isUnlocked()) c++; } return c;
            }
            case MAX_COMBO: return maxComboReached;
            default: return 0;
        }
    }

    public List<Achievement> getAndClearRecentlyUnlocked() {
        List<Achievement> recent = new ArrayList<>(recentlyUnlocked);
        recentlyUnlocked.clear();
        return recent;
    }

    private void checkGeneratorUnlocks() {
        for (Generator gen : generators) {
            gen.tryUnlock(totalCoinsEarned);
        }
    }

    // ==================== OFFLINE ====================

    public double calculateOfflineEarnings(long lastSaveTime) {
        long offlineSeconds = (System.currentTimeMillis() - lastSaveTime) / 1000;
        offlineSeconds = Math.min(offlineSeconds, 8 * 60 * 60);
        if (offlineSeconds > 60) {
            double offlineEarnings = getProductionPerSecond() * offlineSeconds * offlineMultiplier;
            updateMissionProgress(DailyMission.MissionType.COLLECT_OFFLINE, 1);
            return offlineEarnings;
        }
        return 0;
    }

    public void addOfflineEarnings(double earnings) {
        coins += earnings;
        totalCoinsEarned += earnings;
        globalTotalCoinsEarned += earnings;
    }

    // ==================== SISTEMA DE TRABAJADORES ====================

    private void processSalaries(long currentTime) {
        if (currentTime - lastSalaryPayTime < SALARY_INTERVAL_MS) return;
        lastSalaryPayTime = currentTime;

        totalMonthlySalary = 0;
        List<Worker> toRemove = new ArrayList<>();
        int unpaidCount = 0;

        for (Worker worker : ownedWorkers) {
            double salary = worker.getMonthlySalary();
            totalMonthlySalary += salary;

            if (coins >= salary) {
                coins -= salary;
                worker.tickMood(true);
            } else {
                worker.tickMood(false);
                unpaidCount++;
            }

            if (worker.shouldQuit()) {
                toRemove.add(worker);
            }
        }

        // Mass strike: if >40% unpaid (adjusted by event modifiers), all angry workers join strike
        double strikeThreshold = 0.4 - getEventStrikeChanceModifier(); // events can lower/raise threshold
        strikeThreshold = Math.max(0.15, Math.min(0.6, strikeThreshold));
        if (ownedWorkers.size() > 0 && (double) unpaidCount / ownedWorkers.size() > strikeThreshold) {
            for (Worker w : ownedWorkers) {
                if (w.getMood() == Worker.WorkerMood.ANGRY && w.hasStrikeRights()) {
                    w.setMood(Worker.WorkerMood.ON_STRIKE);
                }
            }
        }

        // === WELFARE → WORKER MOOD CONNECTION ===
        // Low nursery welfare stresses ALL workers (they see neglected pets)
        if (nurseryWelfareLevel < 40) {
            double stressChance = (40 - nurseryWelfareLevel) / 100.0; // up to 40% chance
            for (Worker w : ownedWorkers) {
                if (w.getMood() == Worker.WorkerMood.HAPPY && random.nextDouble() < stressChance * 0.3) {
                    w.tickMood(false); // mood drops
                }
                // Nursery workers especially affected
                if (NURSERY_ASSIGNMENT_ID.equals(w.getAssignedGeneratorId()) && nurseryWelfareLevel < 25) {
                    if (w.getMood() != Worker.WorkerMood.ON_STRIKE) {
                        w.tickMood(false); // extra mood penalty
                    }
                }
            }
        }
        // High welfare boosts morale slightly
        if (nurseryWelfareLevel > 80) {
            for (Worker w : ownedWorkers) {
                if (w.getMood() == Worker.WorkerMood.ANGRY && random.nextDouble() < 0.1) {
                    w.tickMood(true); // small chance of mood recovery
                }
            }
        }

        ownedWorkers.removeAll(toRemove);
    }

    private void updateWorkerProductivity() {
        for (Generator gen : generators) {
            List<Worker> assigned = getWorkersForGenerator(gen.getId());
            if (assigned.isEmpty()) {
                gen.setAssignedWorkerProductivity(0.0);
                continue;
            }
            double totalProd = 0;
            for (Worker w : assigned) {
                totalProd += w.getEffectiveProductivity();
            }
            // Average productivity * count factor (more workers = more bonus)
            double avgProd = totalProd / assigned.size();
            double bonus = avgProd * (1.0 + (assigned.size() - 1) * 0.15);
            gen.setAssignedWorkerProductivity(bonus);
        }
    }

    public Worker buyWorkerBox(WorkerBox.BoxType type) {
        double cost = WorkerBox.getCost(type, getProductionPerSecond());
        if (coins < cost) return null;
        coins -= cost;
        Worker worker = WorkerBox.open(type, getProductionPerSecond());
        ownedWorkers.add(worker);
        updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
        return worker;
    }

    public boolean assignWorker(String workerId, int generatorIndex) {
        if (generatorIndex < 0 || generatorIndex >= generators.size()) return false;
        Generator gen = generators.get(generatorIndex);
        if (!gen.isUnlocked() || gen.getOwned() == 0) return false;

        // Check capacity
        int currentAssigned = getWorkersForGenerator(gen.getId()).size();
        if (currentAssigned >= gen.getMaxWorkerCapacity()) return false;

        for (Worker w : ownedWorkers) {
            if (w.getId().equals(workerId)) {
                w.setAssignedGeneratorId(gen.getId());
                return true;
            }
        }
        return false;
    }

    public boolean unassignWorker(String workerId) {
        for (Worker w : ownedWorkers) {
            if (w.getId().equals(workerId)) {
                w.setAssignedGeneratorId(null);
                return true;
            }
        }
        return false;
    }

    public boolean fireWorker(String workerId) {
        return ownedWorkers.removeIf(w -> w.getId().equals(workerId));
    }

    public boolean openBranch(int generatorIndex) {
        if (generatorIndex < 0 || generatorIndex >= generators.size()) return false;
        Generator gen = generators.get(generatorIndex);
        if (!gen.canOpenBranch() || gen.getOwned() == 0) return false;
        double cost = gen.getBranchCost();
        if (coins < cost) return false;
        coins -= cost;
        gen.setBranches(gen.getBranches() + 1);
        updateMissionProgress(DailyMission.MissionType.SPEND_COINS, cost);
        return true;
    }

    public List<Worker> getOwnedWorkers() { return ownedWorkers; }
    public void setOwnedWorkers(List<Worker> workers) { this.ownedWorkers = workers; }

    public List<Worker> getUnassignedWorkers() {
        List<Worker> result = new ArrayList<>();
        for (Worker w : ownedWorkers) {
            if (!w.isAssigned()) result.add(w);
        }
        return result;
    }

    public List<Worker> getWorkersForGenerator(String generatorId) {
        List<Worker> result = new ArrayList<>();
        for (Worker w : ownedWorkers) {
            if (generatorId.equals(w.getAssignedGeneratorId())) result.add(w);
        }
        return result;
    }

    public double getTotalMonthlySalary() {
        double total = 0;
        for (Worker w : ownedWorkers) total += w.getMonthlySalary();
        return total;
    }

    public long getLastSalaryPayTime() { return lastSalaryPayTime; }
    public void setLastSalaryPayTime(long t) { this.lastSalaryPayTime = t; }

    // === CONTRACT TEMPLATES ===
    public List<Worker.ContractTemplate> getContractTemplates() { return contractTemplates; }
    public void setContractTemplates(List<Worker.ContractTemplate> templates) { this.contractTemplates = templates; }

    public void addContractTemplate(Worker.ContractTemplate template) {
        contractTemplates.add(template);
    }

    public void removeContractTemplate(String templateId) {
        contractTemplates.removeIf(t -> t.id.equals(templateId));
    }

    /**
     * Negotiate a contract with a newly opened worker.
     * Returns: 0 = accepted, 1 = rejected (can retry once), 2 = final rejection
     */
    public int negotiateContract(Worker worker, Worker.ContractTemplate contract) {
        double acceptance = worker.evaluateContract(contract);
        Random rand = new Random();
        boolean accepted = rand.nextDouble() < acceptance;
        if (accepted) {
            worker.applyContract(contract);
            return 0; // accepted
        }
        return 1; // rejected, player can retry
    }

    /** Resolve a strike for a single worker with new contract */
    public boolean resolveWorkerStrike(String workerId, Worker.ContractTemplate contract) {
        for (Worker w : ownedWorkers) {
            if (w.getId().equals(workerId)) {
                return w.resolveStrike(contract);
            }
        }
        return false;
    }

    /** Resolve mass strike - apply contract to all on-strike workers */
    public int resolveMassStrike(Worker.ContractTemplate contract) {
        int resolved = 0;
        for (Worker w : ownedWorkers) {
            if (w.getMood() == Worker.WorkerMood.ON_STRIKE) {
                if (w.resolveStrike(contract)) resolved++;
            }
        }
        totalStrikesResolved += resolved;
        if (resolved > 0) checkAchievements();
        return resolved;
    }

    // ==================== NURSERY SYSTEM ====================

    public double getNurseryWelfareLevel() { return nurseryWelfareLevel; }
    public void setNurseryWelfareLevel(double l) { this.nurseryWelfareLevel = Math.max(0, Math.min(100, l)); }
    public long getLastWelfareDecayTime() { return lastWelfareDecayTime; }
    public void setLastWelfareDecayTime(long t) { this.lastWelfareDecayTime = t; }

    /** How many workers are assigned to the nursery */
    public int getNurseryWorkerCount() {
        int count = 0;
        for (Worker w : ownedWorkers) {
            if (NURSERY_ASSIGNMENT_ID.equals(w.getAssignedGeneratorId())) count++;
        }
        return count;
    }

    /** How many workers are needed for current pet count (1 per 4 pets) */
    public int getNurseryWorkersNeeded() {
        int ownedCount = 0;
        for (Pet p : pets) { if (p.isOwned() && p.isAlive()) ownedCount++; }
        return (int) Math.ceil(ownedCount / 4.0);
    }

    /** Assign a worker to the nursery */
    public boolean assignWorkerToNursery(String workerId) {
        for (Worker w : ownedWorkers) {
            if (w.getId().equals(workerId)) {
                w.setAssignedGeneratorId(NURSERY_ASSIGNMENT_ID);
                return true;
            }
        }
        return false;
    }

    /** Update nursery welfare based on pet stats and workers */
    public void updateNurseryWelfare() {
        int ownedAliveCount = 0;
        double totalStats = 0;
        for (Pet p : pets) {
            if (p.isOwned() && p.isAlive()) {
                ownedAliveCount++;
                totalStats += (p.getHealth() + p.getHappiness() + (100 - p.getHunger())
                        + p.getEnergy() + p.getMentalHealth() + p.getHygiene()) / 6.0;
            }
        }

        if (ownedAliveCount == 0) { nurseryWelfareLevel = 70; return; }

        double avgStats = totalStats / ownedAliveCount;
        int nurseryWorkers = getNurseryWorkerCount();
        int workersNeeded = getNurseryWorkersNeeded();

        // Workers provide auto-care: if enough workers, welfare trends toward avgStats
        double workerCoverage = workersNeeded > 0 ? Math.min(1.0, (double) nurseryWorkers / workersNeeded) : 0;

        // Target welfare: avg of pet stats, boosted by worker coverage
        double targetWelfare = avgStats * (0.5 + 0.5 * workerCoverage);

        // Smooth approach
        nurseryWelfareLevel += (targetWelfare - nurseryWelfareLevel) * 0.05;

        // Workers also auto-care pets (feed, clean, etc.)
        if (nurseryWorkers > 0) {
            int petsPerWorker = Math.max(1, ownedAliveCount / nurseryWorkers);
            int autoCaredPets = 0;
            for (Pet p : pets) {
                if (p.isOwned() && p.isAlive() && autoCaredPets < nurseryWorkers * 4) {
                    // Auto-feed if hungry
                    if (p.getHunger() > 50) p.setHunger(Math.max(10, p.getHunger() - 15));
                    // Auto-clean
                    if (p.getHygiene() < 50) p.setHygiene(Math.min(90, p.getHygiene() + 15));
                    // Auto-play if low energy/happiness
                    if (p.getHappiness() < 50) p.setHappiness(Math.min(80, p.getHappiness() + 10));
                    if (p.getEnergy() < 30) p.setEnergy(Math.min(70, p.getEnergy() + 10));
                    autoCaredPets++;
                }
            }
        }

        nurseryWelfareLevel = Math.max(0, Math.min(100, nurseryWelfareLevel));
    }

    /** Get the welfare multiplier for pet bonuses (0.0 - 1.0) */
    public double getWelfareMultiplier() {
        return nurseryWelfareLevel / 100.0;
    }

    /** Breed two pets — offspring goes into incubation */
    public Pet breedPets(int index1, int index2) {
        if (index1 == index2) return null;
        if (index1 < 0 || index1 >= pets.size() || index2 < 0 || index2 >= pets.size()) return null;

        Pet p1 = pets.get(index1);
        Pet p2 = pets.get(index2);

        if (!p1.canBreed() || !p2.canBreed()) return null;

        // Check max pets
        int ownedCount = 0;
        for (Pet p : pets) { if (p.isOwned()) ownedCount++; }
        if (ownedCount >= Pet.MAX_PETS) return null;

        // Check cost
        double cost = Pet.getBreedCost(p1, p2);
        if (coins < cost) return null;

        coins -= cost;
        Pet offspring = Pet.breed(p1, p2);
        pets.add(offspring);

        totalPetsBred++;
        if (offspring.hadMutation()) {
            totalRareMutations++;
        }
        checkAchievements();
        return offspring;
    }

    /** Check and hatch any pets whose incubation is complete. Returns list of newly hatched. */
    public List<Pet> checkIncubation() {
        List<Pet> hatched = new ArrayList<>();
        for (Pet p : pets) {
            if (p.isIncubating() && p.isReadyToHatch()) {
                p.hatch();
                hatched.add(p);
            }
        }
        if (!hatched.isEmpty()) checkAchievements();
        return hatched;
    }

    /** Get all currently incubating pets */
    public List<Pet> getIncubatingPets() {
        List<Pet> result = new ArrayList<>();
        for (Pet p : pets) {
            if (p.isIncubating()) result.add(p);
        }
        return result;
    }

    // Tracking getters/setters
    public int getTotalPetsBred() { return totalPetsBred; }
    public void setTotalPetsBred(int v) { this.totalPetsBred = v; }
    public int getTotalRareMutations() { return totalRareMutations; }
    public void setTotalRareMutations(int v) { this.totalRareMutations = v; }
    public int getTotalStrikesResolved() { return totalStrikesResolved; }
    public void setTotalStrikesResolved(int v) { this.totalStrikesResolved = v; }
    public void setMaxComboReached(int v) { this.maxComboReached = v; }

    // ==================== EVENT SYSTEM ====================

    public void updateGalacticStability() {
        double strikePercent = 0;
        if (!ownedWorkers.isEmpty()) {
            int onStrike = 0;
            for (Worker w : ownedWorkers) {
                if (w.getMood() == Worker.WorkerMood.ON_STRIKE || w.getMood() == Worker.WorkerMood.ANGRY) onStrike++;
            }
            strikePercent = (double) onStrike / ownedWorkers.size();
        }
        double raw = 0.35 * nurseryWelfareLevel
                + 0.25 * (100.0 - strikePercent * 100.0)
                + 0.20 * Math.min(prestigeLevel * 5.0, 100.0)
                + 0.20 * 60.0; // baseline
        galacticStability += (raw - galacticStability) * 0.02;
        galacticStability = Math.max(0, Math.min(100, galacticStability));
    }

    /** Check event triggers — call from game loop (throttled internally) */
    public GameEvent checkEventTriggers() {
        eventCheckCounter++;
        if (eventCheckCounter < 100) return null; // every 5 sec (100 ticks * 50ms)
        eventCheckCounter = 0;

        // Expire finished events — auto-resolve unresolved ones with worst choice, then move to history
        List<GameEvent> autoResolvedEvents = new ArrayList<>();
        java.util.Iterator<GameEvent> it = activeGameEvents.iterator();
        while (it.hasNext()) {
            GameEvent ev = it.next();
            if (!ev.isActive()) {
                // If event has choices and player didn't choose, apply worst penalty
                if (ev.getType().hasChoices && ev.getChoiceIndex() < 0 && !ev.getChoices().isEmpty()) {
                    java.util.Map<String, Double> effects = ev.autoResolveWithWorst();
                    applyImmediateEffects(effects);
                    autoResolvedEvents.add(ev);
                }
                eventHistory.add(ev);
                if (eventHistory.size() > 50) eventHistory.remove(0);
                it.remove();
            }
        }
        // Store auto-resolved events for notification
        if (!autoResolvedEvents.isEmpty()) {
            lastAutoResolvedEvents.clear();
            lastAutoResolvedEvents.addAll(autoResolvedEvents);
        }

        if (activeGameEvents.size() >= MAX_ACTIVE_EVENTS) return null;

        long now = System.currentTimeMillis();

        // Check conditional triggers first
        GameEvent triggered = checkConditionalTriggers(now);
        if (triggered != null) return triggered;

        // Random weighted triggers
        return checkRandomTriggers(now);
    }

    /** Check if an event type is eligible given current game state (workers/pets) */
    private boolean isEventEligible(GameEvent.EventType type) {
        if (type.requiresWorkers && ownedWorkers.isEmpty()) return false;
        if (type.requiresPets) {
            boolean hasPets = false;
            for (Pet p : pets) { if (p.isOwned()) { hasPets = true; break; } }
            if (!hasPets) return false;
        }
        return true;
    }

    /** Pick an eligible event from a pool, returns null if none eligible */
    private GameEvent.EventType pickEligibleEvent(GameEvent.EventType[] pool) {
        // Shuffle to avoid bias
        java.util.List<GameEvent.EventType> candidates = new java.util.ArrayList<>();
        for (GameEvent.EventType t : pool) {
            if (isEventEligible(t) && !hasActiveEventOfType(t)) candidates.add(t);
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    private GameEvent checkConditionalTriggers(long now) {
        // Animal welfare scandal — welfare < 30 (only if has pets)
        if (isEventEligible(GameEvent.EventType.ANIMAL_WELFARE_SCANDAL)
                && nurseryWelfareLevel < 30 && !hasActiveEventOfType(GameEvent.EventType.ANIMAL_WELFARE_SCANDAL)
                && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
            if (random.nextDouble() < 0.3) return spawnEvent(GameEvent.EventType.ANIMAL_WELFARE_SCANDAL, now);
        }
        // Union movement — >30% workers angry/strike (only if has workers)
        if (isEventEligible(GameEvent.EventType.UNION_MOVEMENT) && !ownedWorkers.isEmpty()) {
            int angry = 0;
            for (Worker w : ownedWorkers) {
                if (w.getMood() == Worker.WorkerMood.ANGRY || w.getMood() == Worker.WorkerMood.ON_STRIKE) angry++;
            }
            if ((double) angry / ownedWorkers.size() > 0.3 && !hasActiveEventOfType(GameEvent.EventType.UNION_MOVEMENT)
                    && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
                if (random.nextDouble() < 0.25) return spawnEvent(GameEvent.EventType.UNION_MOVEMENT, now);
            }
        }
        // Sector strike — >2 workers on strike
        if (isEventEligible(GameEvent.EventType.SECTOR_STRIKE) && !ownedWorkers.isEmpty()) {
            int strikes = 0;
            for (Worker w : ownedWorkers) { if (w.getMood() == Worker.WorkerMood.ON_STRIKE) strikes++; }
            if (strikes > 2 && !hasActiveEventOfType(GameEvent.EventType.SECTOR_STRIKE)
                    && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
                if (random.nextDouble() < 0.2) return spawnEvent(GameEvent.EventType.SECTOR_STRIKE, now);
            }
        }
        // Labor shortage — few workers + many generators
        if (isEventEligible(GameEvent.EventType.LABOR_SHORTAGE) && ownedWorkers.size() < 3
                && generators.size() > 5 && !hasActiveEventOfType(GameEvent.EventType.LABOR_SHORTAGE)
                && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
            if (random.nextDouble() < 0.12) return spawnEvent(GameEvent.EventType.LABOR_SHORTAGE, now);
        }
        // Company of year — high stability + welfare
        if (galacticStability > 75 && nurseryWelfareLevel > 80
                && !hasActiveEventOfType(GameEvent.EventType.COMPANY_OF_YEAR)
                && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
            if (random.nextDouble() < 0.15) return spawnEvent(GameEvent.EventType.COMPANY_OF_YEAR, now);
        }
        // Crisis — low stability
        if (galacticStability < 30 && now - lastMajorEventTime > MAJOR_EVENT_COOLDOWN
                && !hasActiveEventOfType(GameEvent.EventType.GLOBAL_ECONOMIC_CRISIS)) {
            if (random.nextDouble() < 0.25) return spawnEvent(GameEvent.EventType.GLOBAL_ECONOMIC_CRISIS, now);
        }
        // Golden age — very high stability (rare)
        if (galacticStability > 85 && now - lastMajorEventTime > MAJOR_EVENT_COOLDOWN
                && !hasActiveEventOfType(GameEvent.EventType.GOLDEN_AGE)) {
            if (random.nextDouble() < 0.08) return spawnEvent(GameEvent.EventType.GOLDEN_AGE, now);
        }
        // Market crash — very low stability
        if (galacticStability < 20 && now - lastMajorEventTime > MAJOR_EVENT_COOLDOWN
                && !hasActiveEventOfType(GameEvent.EventType.MARKET_CRASH)) {
            if (random.nextDouble() < 0.15) return spawnEvent(GameEvent.EventType.MARKET_CRASH, now);
        }
        // Mutant outbreak — high mutation activity (only if has pets)
        if (isEventEligible(GameEvent.EventType.MUTANT_OUTBREAK) && totalRareMutations > 3
                && !hasActiveEventOfType(GameEvent.EventType.MUTANT_OUTBREAK)
                && now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
            if (random.nextDouble() < 0.10) return spawnEvent(GameEvent.EventType.MUTANT_OUTBREAK, now);
        }
        return null;
    }

    private GameEvent checkRandomTriggers(long now) {
        // Minor random
        if (now - lastMinorEventTime > MINOR_EVENT_COOLDOWN) {
            double roll = random.nextDouble();
            double threshold = galacticStability > 50 ? 0.08 : 0.12; // more events when unstable
            if (roll < threshold) {
                GameEvent.EventType[] minorPool;
                if (galacticStability < 40) {
                    minorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.RESOURCE_SHORTAGE, GameEvent.EventType.CORRUPTION_SCANDAL,
                            GameEvent.EventType.COMPETITOR_INVASION, GameEvent.EventType.SECTOR_STRIKE,
                            GameEvent.EventType.ENERGY_BLACKOUT, GameEvent.EventType.CYBER_ATTACK,
                            GameEvent.EventType.SUPPLY_CHAIN_CRISIS, GameEvent.EventType.TAX_AUDIT,
                            GameEvent.EventType.ESPIONAGE, GameEvent.EventType.SPACE_DEBRIS};
                } else if (galacticStability > 60) {
                    minorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.SCIENTIFIC_DISCOVERY, GameEvent.EventType.TECH_BREAKTHROUGH,
                            GameEvent.EventType.COMPANY_OF_YEAR, GameEvent.EventType.RESOURCE_SHORTAGE,
                            GameEvent.EventType.TERRAFORMING_SUCCESS, GameEvent.EventType.INNOVATION_GRANT,
                            GameEvent.EventType.WORKER_TRAINING, GameEvent.EventType.PET_COMPETITION,
                            GameEvent.EventType.INVESTOR_INTEREST};
                } else {
                    minorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.RESOURCE_SHORTAGE, GameEvent.EventType.SCIENTIFIC_DISCOVERY,
                            GameEvent.EventType.COMPETITOR_INVASION, GameEvent.EventType.CORRUPTION_SCANDAL,
                            GameEvent.EventType.ENERGY_BLACKOUT, GameEvent.EventType.SPACE_DEBRIS,
                            GameEvent.EventType.TAX_AUDIT, GameEvent.EventType.LABOR_SHORTAGE,
                            GameEvent.EventType.MUTANT_OUTBREAK};
                }
                GameEvent.EventType pick = pickEligibleEvent(minorPool);
                if (pick != null) return spawnEvent(pick, now);
            }
        }
        // Major random
        if (now - lastMajorEventTime > MAJOR_EVENT_COOLDOWN) {
            double roll = random.nextDouble();
            if (roll < 0.06) {
                GameEvent.EventType[] majorPool;
                if (currentWorldIndex >= 5) {
                    majorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.GLOBAL_ECONOMIC_BOOM, GameEvent.EventType.LEGAL_REFORM,
                            GameEvent.EventType.PANDEMIC_OUTBREAK, GameEvent.EventType.ALIEN_CONTACT,
                            GameEvent.EventType.GALACTIC_WAR, GameEvent.EventType.TRADE_AGREEMENT,
                            GameEvent.EventType.CLIMATE_DISASTER, GameEvent.EventType.MARKET_CRASH,
                            GameEvent.EventType.GALACTIC_FESTIVAL, GameEvent.EventType.DIPLOMATIC_INCIDENT,
                            GameEvent.EventType.DARK_MATTER_SURGE};
                } else if (currentWorldIndex >= 3) {
                    majorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.GLOBAL_ECONOMIC_BOOM, GameEvent.EventType.LEGAL_REFORM,
                            GameEvent.EventType.PANDEMIC_OUTBREAK, GameEvent.EventType.TRADE_AGREEMENT,
                            GameEvent.EventType.CLIMATE_DISASTER, GameEvent.EventType.GALACTIC_FESTIVAL,
                            GameEvent.EventType.DIPLOMATIC_INCIDENT};
                } else {
                    majorPool = new GameEvent.EventType[]{
                            GameEvent.EventType.GLOBAL_ECONOMIC_BOOM, GameEvent.EventType.LEGAL_REFORM,
                            GameEvent.EventType.PANDEMIC_OUTBREAK, GameEvent.EventType.GALACTIC_FESTIVAL};
                }
                GameEvent.EventType pick = pickEligibleEvent(majorPool);
                if (pick != null) return spawnEvent(pick, now);
            }
        }
        return null;
    }

    private GameEvent spawnEvent(GameEvent.EventType type, long now) {
        GameEvent event = GameEvent.create(type);
        activeGameEvents.add(event);
        if (type.isMajor()) lastMajorEventTime = now;
        else lastMinorEventTime = now;
        return event;
    }

    private boolean hasActiveEventOfType(GameEvent.EventType type) {
        for (GameEvent e : activeGameEvents) { if (e.getType() == type && e.isActive()) return true; }
        return false;
    }

    /** Resolve an event with the player's choice, apply immediate effects */
    public void resolveEvent(String eventId, int choiceIndex) {
        for (GameEvent e : activeGameEvents) {
            if (e.getId().equals(eventId) && e.isActive()) {
                java.util.Map<String, Double> effects = e.resolve(choiceIndex);
                applyImmediateEffects(effects);
                // Move to history
                eventHistory.add(e);
                if (eventHistory.size() > 50) eventHistory.remove(0);
                break;
            }
        }
        checkAchievements();
    }

    private void applyImmediateEffects(java.util.Map<String, Double> effects) {
        if (effects.containsKey(GameEvent.EFF_STABILITY)) {
            galacticStability = Math.max(0, Math.min(100,
                    galacticStability + effects.get(GameEvent.EFF_STABILITY)));
        }
        if (effects.containsKey(GameEvent.EFF_WELFARE)) {
            nurseryWelfareLevel = Math.max(0, Math.min(100,
                    nurseryWelfareLevel + effects.get(GameEvent.EFF_WELFARE)));
        }
        if (effects.containsKey(GameEvent.EFF_COINS_PERCENT)) {
            double pct = effects.get(GameEvent.EFF_COINS_PERCENT);
            coins += coins * pct;
            if (coins < 0) coins = 0;
        }
        if (effects.containsKey(GameEvent.EFF_COINS_FLAT)) {
            coins += effects.get(GameEvent.EFF_COINS_FLAT);
            if (coins < 0) coins = 0;
        }
        if (effects.containsKey(GameEvent.EFF_MOOD_ALL)) {
            boolean positive = effects.get(GameEvent.EFF_MOOD_ALL) > 0;
            for (Worker w : ownedWorkers) { w.tickMood(positive); }
        }
    }

    /** Get total production multiplier from all active events */
    public double getEventProductionMultiplier() {
        double mult = 0;
        for (GameEvent e : activeGameEvents) {
            if (e.isActive()) mult += e.getEffect(GameEvent.EFF_PRODUCTION_MULT);
        }
        return mult;
    }

    /** Get total strike chance modifier from active events */
    public double getEventStrikeChanceModifier() {
        double mod = 0;
        for (GameEvent e : activeGameEvents) {
            if (e.isActive()) mod += e.getEffect(GameEvent.EFF_STRIKE_CHANCE);
        }
        return mod;
    }

    /** Get total mutation chance modifier from active events */
    public double getEventMutationModifier() {
        double mod = 0;
        for (GameEvent e : activeGameEvents) {
            if (e.isActive()) mod += e.getEffect(GameEvent.EFF_MUTATION_CHANCE);
        }
        return mod;
    }

    // Event getters
    public double getGalacticStability() { return galacticStability; }
    public void setGalacticStability(double s) { this.galacticStability = s; }
    public List<GameEvent> getActiveGameEvents() { return activeGameEvents; }
    public void setActiveGameEvents(List<GameEvent> e) { this.activeGameEvents = e; }
    public List<GameEvent> getEventHistory() { return eventHistory; }
    public void setEventHistory(List<GameEvent> h) { this.eventHistory = h; }
    public long getLastMinorEventTime() { return lastMinorEventTime; }
    public void setLastMinorEventTime(long t) { this.lastMinorEventTime = t; }
    public long getLastMajorEventTime() { return lastMajorEventTime; }
    public void setLastMajorEventTime(long t) { this.lastMajorEventTime = t; }

    /** Get and clear recently auto-resolved events (for UI notification) */
    public List<GameEvent> getAndClearAutoResolvedEvents() {
        List<GameEvent> result = new ArrayList<>(lastAutoResolvedEvents);
        lastAutoResolvedEvents.clear();
        return result;
    }

    // ==================== FORMATEO ====================

    public static String fmt(double n) {
        if (n < 1000) return String.format("%.0f", n);
        if (n < 1_000_000) return String.format("%.1fK", n / 1000);
        if (n < 1_000_000_000) return String.format("%.2fM", n / 1_000_000);
        if (n < 1_000_000_000_000L) return String.format("%.2fB", n / 1_000_000_000);
        if (n < 1_000_000_000_000_000L) return String.format("%.2fT", n / 1_000_000_000_000L);
        if (n < 1_000_000_000_000_000_000L) return String.format("%.2fQa", n / 1_000_000_000_000_000L);
        if (n < 1e21) return String.format("%.2fQi", n / 1e18);
        if (n < 1e24) return String.format("%.2fSx", n / 1e21);
        if (n < 1e27) return String.format("%.2fSp", n / 1e24);
        if (n < 1e30) return String.format("%.2fOc", n / 1e27);
        return String.format("%.2fNo", n / 1e30);
    }

    // ==================== GETTERS / SETTERS ====================

    public double getCoins() { return coins; }
    public double getTotalCoinsEarned() { return totalCoinsEarned; }
    public long getTotalTaps() { return totalTaps; }
    public long getTotalCriticalHits() { return totalCriticalHits; }
    public double getPerTap() { return calculateTapValue(); }
    public double getCriticalChance() { return criticalChance; }
    public double getCriticalMultiplier() { return criticalMultiplier; }
    public int getPrestigeLevel() { return prestigeLevel; }
    public double getPrestigePoints() { return prestigePoints; }
    public List<Generator> getGenerators() { return generators; }
    public List<Upgrade> getUpgrades() { return upgrades; }
    public List<Achievement> getAchievements() { return achievements; }
    public List<World> getWorlds() { return worlds; }
    public int getCurrentWorldIndex() { return currentWorldIndex; }
    public List<MiniGame> getMiniGames() { return miniGames; }
    public List<DailyMission> getDailyMissions() { return dailyMissions; }
    public List<Pet> getPets() { return pets; }
    public Pet getActivePet() { return activePet; }
    public int getActivePetIndex() {
        if (activePet == null) return -1;
        return pets.indexOf(activePet);
    }
    public ComboSystem getComboSystem() { return comboSystem; }
    public int getMaxComboReached() { return maxComboReached; }
    public long getLastUpdateTime() { return lastUpdateTime; }
    public int getTotalGeneratorsBought() { return totalGeneratorsBought; }
    public int getTotalMiniGamesPlayed() { return totalMiniGamesPlayed; }
    public int getTotalEventsCompleted() { return totalEventsCompleted; }

    // Global stats (never reset)
    public double getGlobalTotalCoinsEarned() { return globalTotalCoinsEarned; }
    public void setGlobalTotalCoinsEarned(double v) { this.globalTotalCoinsEarned = v; }
    public long getGlobalTotalTaps() { return globalTotalTaps; }
    public void setGlobalTotalTaps(long v) { this.globalTotalTaps = v; }
    public int getGlobalTotalGeneratorsBought() { return globalTotalGeneratorsBought; }
    public void setGlobalTotalGeneratorsBought(int v) { this.globalTotalGeneratorsBought = v; }
    public int getTotalPrestigesPerformed() { return totalPrestigesPerformed; }
    public void setTotalPrestigesPerformed(int v) { this.totalPrestigesPerformed = v; }
    public long getGlobalTotalCriticalHits() { return globalTotalCriticalHits; }
    public void setGlobalTotalCriticalHits(long v) { this.globalTotalCriticalHits = v; }

    public void setCoins(double c) { this.coins = c; }
    public void setTotalCoinsEarned(double c) { this.totalCoinsEarned = c; }

    // Monetization
    public MonetizationManager getMonetization() { return monetization; }
    public void setMonetization(MonetizationManager m) { this.monetization = m; }
    public void setTotalTaps(long t) { this.totalTaps = t; }
    public void setPrestigeLevel(int l) { this.prestigeLevel = l; }
    public void setPrestigePoints(double p) { this.prestigePoints = p; }
    public void setLastUpdateTime(long t) { this.lastUpdateTime = t; }
    public void setCurrentWorldIndex(int i) { this.currentWorldIndex = i; }
    public void setTotalMiniGamesPlayed(int t) { this.totalMiniGamesPlayed = t; }
    public long getLastMissionResetTime() { return lastMissionResetTime; }
    public void setLastMissionResetTime(long t) { this.lastMissionResetTime = t; }
    public void setDailyMissions(List<DailyMission> missions) { this.dailyMissions = missions; }

    // Compatibilidad
    public long getUpgradeCost() { return 50; }
    public void setPerTap(long p) { this.basePerTap = p; }
    public void setUpgradeCost(long c) { }
    public boolean buyPerTapUpgrade() { return buyUpgrade(0); }

    // ==================== CLASE RESULTADO DE TAP ====================

    public static class TapResult {
        public final double amount;
        public final boolean isCritical;
        public final int comboCount;
        public final double comboMultiplier;

        public TapResult(double amount, boolean isCritical) {
            this(amount, isCritical, 0, 1.0);
        }

        public TapResult(double amount, boolean isCritical, int comboCount, double comboMultiplier) {
            this.amount = amount;
            this.isCritical = isCritical;
            this.comboCount = comboCount;
            this.comboMultiplier = comboMultiplier;
        }
    }
}
