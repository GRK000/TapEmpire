package com.example.juego;

import java.util.Random;

/**
 * Sistema Gacha de Trabajadores — Cajas aleatorias para obtener workers
 */
public class WorkerBox {

    public enum BoxType {
        BASIC("📦", 50000, new double[]{0.65, 0.28, 0.06, 0.01}),
        PREMIUM("💎", 800000, new double[]{0.10, 0.50, 0.32, 0.08}),
        LEGENDARY("👑", 10000000, new double[]{0.0, 0.0, 0.80, 0.20});

        public final String emoji;
        public final double baseCost;
        public final double[] rarityChances; // COMMON, RARE, EPIC, LEGENDARY

        BoxType(String emoji, double baseCost, double[] rarityChances) {
            this.emoji = emoji;
            this.baseCost = baseCost;
            this.rarityChances = rarityChances;
        }
    }

    private static final Random random = new Random();

    // Name pools — massive variety for collection system
    private static final String[] FIRST_NAMES = {
        "Alex", "Jordan", "Morgan", "Casey", "Riley", "Taylor", "Quinn", "Avery",
        "Harper", "Skyler", "Sage", "Blake", "Drew", "Jamie", "Cameron", "Rowan",
        "Dakota", "Emery", "Finley", "Kai", "Luna", "Nova", "Zara", "Max",
        "Leo", "Aria", "Milo", "Iris", "Felix", "Maya", "Oscar", "Ruby",
        "Hugo", "Cleo", "Atlas", "Jade", "Theo", "Nora", "Ezra", "Wren",
        "Ivan", "Yuki", "Sven", "Lara", "Chen", "Amir", "Olga", "Marco",
        "Elena", "Rafael", "Suki", "Dmitri", "Ingrid", "Kenji", "Lena", "Paolo",
        "Astrid", "Raj", "Freya", "Hiro", "Sofia", "Nikolai", "Mei", "Carlos",
        "Valentina", "Akira", "Celeste", "Omar", "Bianca", "Tariq", "Vivienne", "Ryu",
        "Amara", "Viktor", "Daphne", "Hassan", "Lydia", "Takeshi", "Petra", "Karim",
        "Eloise", "Dante", "Sakura", "Mateo", "Indira", "Leif", "Xiomara", "Abel",
        "Isolde", "Javier", "Kamila", "Renzo", "Thalia", "Idris", "Linnea", "Cosmo",
        "Serena", "Blaise", "Anya", "Cyrus", "Maren", "Lucien", "Nerida", "Bastian",
        "Vesper", "Orion", "Calista", "Emilio", "Rhea", "Stellan", "Aurelia", "Phoenix"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Chen", "García", "Kim", "Patel", "Müller", "Dubois", "Tanaka",
        "Silva", "Andersson", "Johansson", "Williams", "Brown", "Lee", "Wilson",
        "Nakamura", "Santos", "Petrov", "Novak", "Kowalski", "Reyes", "Torres",
        "Fischer", "Ivanov", "Eriksson", "Wagner", "Larsson", "Bernard", "Yamamoto",
        "Rossi", "Fernández", "Lopez", "Schmidt", "Suzuki", "Moreira", "Jensen",
        "Okafor", "Nkosi", "Abadi", "Mahmoud", "Volkov", "Bergström", "O'Brien",
        "Delacroix", "Mendoza", "Vargas", "Kruger", "Hashimoto", "Lindqvist", "Moreau",
        "Fitzgerald", "Colombo", "Huerta", "Stein", "Watanabe", "Becker", "Azevedo",
        "Christensen", "Lombardi", "Kuznetsov", "Bergman", "Ortega", "Vasquez", "Klein",
        "Hoffmann", "Matsuda", "Sokolova", "Lindgren", "Navarro", "Esposito", "Brennan",
        "Dubois", "Sandoval", "Takahashi", "Montalvo", "Visconti", "Salazar", "Erickson"
    };

    private static final String[] SECOND_LAST_NAMES = {
        "", "", "", "", "", "", "", "", "", "", // 50% chance of no second last name
        "de la Cruz", "von Stein", "del Río", "van Houten", "di Marco", "O'Connell",
        "de Souza", "van der Berg", "Al-Rashid", "ibn Said", "dos Santos", "de Luca",
        "Pérez", "Martínez", "González", "Rodríguez", "Hernández", "Ruiz", "Jiménez",
        "Sánchez", "Romero", "Díaz"
    };

    private static final String[] MARITAL_STATUSES = {
        "Single", "Married", "Divorced", "Widowed", "Partnered"
    };

    /**
     * Open a worker box and get a random worker
     * @param productionPerSecond current production to scale salary
     */
    public static Worker open(BoxType type, double productionPerSecond) {
        // Determine rarity
        Worker.WorkerRarity rarity = rollRarity(type);

        // Determine class
        Worker.WorkerClass[] classes = Worker.WorkerClass.values();
        Worker.WorkerClass workerClass = classes[random.nextInt(classes.length)];

        // Determine role based on rarity
        Worker.WorkerRole role = rollRole(rarity);

        // Generate stats
        int productivity = rarity.minProductivity +
                random.nextInt(rarity.maxProductivity - rarity.minProductivity + 1);

        int experience = role.minExperience + random.nextInt(5 + rarity.ordinal() * 3);

        // Base salary scales with production
        double baseSalary = Math.max(10, productionPerSecond * 0.5) *
                (0.8 + random.nextDouble() * 0.4);

        // Generate name (with optional second last name)
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String secondLast = SECOND_LAST_NAMES[random.nextInt(SECOND_LAST_NAMES.length)];
        String name = secondLast.isEmpty() ? firstName + " " + lastName
                : firstName + " " + lastName + " " + secondLast;

        Worker worker = new Worker(name, workerClass, rarity, role, experience,
                productivity, baseSalary);

        // Random personal details
        worker.setMaritalStatus(MARITAL_STATUSES[random.nextInt(MARITAL_STATUSES.length)]);
        if (worker.getMaritalStatus().equals("Married") || worker.getMaritalStatus().equals("Partnered")) {
            worker.setNumberOfChildren(random.nextInt(4));
        }

        // Strike rights: legendaries never strike, commons often have rights
        worker.setStrikeRights(rarity != Worker.WorkerRarity.LEGENDARY && random.nextDouble() < 0.7);

        // Contract duration
        worker.setContractDurationMonths(6 + random.nextInt(18 + rarity.ordinal() * 6));

        // Education — coherent with worker class and rarity
        worker.setEducation(generateEducation(workerClass, rarity));

        return worker;
    }

    /**
     * Generate education coherent with worker class and rarity.
     * Higher rarity → more likely to have advanced degrees.
     * Class determines field of study.
     */
    private static String generateEducation(Worker.WorkerClass workerClass, Worker.WorkerRarity rarity) {
        // Laborers: mostly vocational or none
        // Scientists: science degrees
        // Engineers: engineering degrees
        // Executives/Managers: business degrees
        // Security: vocational or military

        boolean hasUniversityDegree;
        boolean hasMasters;

        switch (rarity) {
            case LEGENDARY:
                hasUniversityDegree = true;
                hasMasters = random.nextDouble() < 0.85;
                break;
            case EPIC:
                hasUniversityDegree = random.nextDouble() < 0.9;
                hasMasters = random.nextDouble() < 0.5;
                break;
            case RARE:
                hasUniversityDegree = random.nextDouble() < 0.65;
                hasMasters = random.nextDouble() < 0.2;
                break;
            default: // COMMON
                hasUniversityDegree = random.nextDouble() < 0.3;
                hasMasters = false;
                break;
        }

        switch (workerClass) {
            case SCIENTIST:
                if (hasMasters) return pickRandom(new String[]{"PhD Quantum Physics", "PhD Astrophysics", "PhD Biochemistry", "PhD Molecular Biology", "PhD Neuroscience", "MSc Data Science"});
                if (hasUniversityDegree) return pickRandom(new String[]{"BSc Physics", "BSc Chemistry", "BSc Biology", "BSc Mathematics", "BSc Biotechnology", "BSc Environmental Science"});
                return pickRandom(new String[]{"Lab Technician Cert.", "Science Associate Degree", "None"});

            case ENGINEER:
                if (hasMasters) return pickRandom(new String[]{"MSc Mechanical Engineering", "MSc Software Engineering", "MSc Electrical Engineering", "MSc Aerospace Engineering", "MSc Civil Engineering", "MSc Robotics"});
                if (hasUniversityDegree) return pickRandom(new String[]{"BEng Mechanical Engineering", "BEng Software Engineering", "BEng Electrical Engineering", "BEng Industrial Engineering", "BEng Chemical Engineering", "BSc Computer Science"});
                return pickRandom(new String[]{"Vocational: Electronics", "Vocational: Mechanics", "Technical Certificate", "None"});

            case EXECUTIVE:
                if (hasMasters) return pickRandom(new String[]{"MBA Harvard", "MBA Stanford", "MBA INSEAD", "MSc Finance", "MSc Economics", "JD Corporate Law"});
                if (hasUniversityDegree) return pickRandom(new String[]{"BA Business Administration", "BSc Economics", "BA Finance", "BSc Accounting", "BA International Business", "BA Marketing"});
                return pickRandom(new String[]{"Business Administration Diploma", "Sales Certificate", "None"});

            case MANAGER:
                if (hasMasters) return pickRandom(new String[]{"MBA Operations", "MSc Project Management", "MSc Strategic Management", "MBA Leadership", "MA Human Resources"});
                if (hasUniversityDegree) return pickRandom(new String[]{"BA Management", "BSc Operations Management", "BA Human Resources", "BSc Logistics", "BA Communication"});
                return pickRandom(new String[]{"Management Diploma", "Supervisory Certificate", "None"});

            case SECURITY:
                if (hasUniversityDegree) return pickRandom(new String[]{"BSc Criminology", "BA Security Studies", "BSc Forensic Science", "Military Academy"});
                return pickRandom(new String[]{"Vocational: Security Operations", "Military Service", "Private Security License", "First Aid & Emergency Cert.", "None"});

            case LABORER:
                if (hasUniversityDegree) return pickRandom(new String[]{"BA General Studies", "BSc Industrial Technology", "Associate: Construction Mgmt"});
                return pickRandom(new String[]{"Vocational: Welding", "Vocational: Plumbing", "Vocational: Carpentry", "Vocational: Electrician", "Vocational: Heavy Machinery", "Forklift Operator Cert.", "Health & Safety Cert.", "None", "None"});

            default:
                return "None";
        }
    }

    private static String pickRandom(String[] arr) {
        return arr[random.nextInt(arr.length)];
    }

    /**
     * Get the cost of a box, scaled by production (much more expensive now)
     */
    public static double getCost(BoxType type, double productionPerSecond) {
        switch (type) {
            case PREMIUM:
                return Math.max(type.baseCost, productionPerSecond * 600);
            case LEGENDARY:
                return Math.max(type.baseCost, productionPerSecond * 5000);
            default: // BASIC
                return Math.max(type.baseCost, productionPerSecond * 120);
        }
    }

    /** All possible first names for collection */
    public static String[] getAllFirstNames() { return FIRST_NAMES; }
    /** All possible last names for collection */
    public static String[] getAllLastNames() { return LAST_NAMES; }
    /** Total unique possible workers (first * last combinations) */
    public static int getTotalPossibleWorkers() { return FIRST_NAMES.length * LAST_NAMES.length; }

    private static Worker.WorkerRarity  rollRarity(BoxType type) {
        double roll = random.nextDouble();
        double cumulative = 0;
        Worker.WorkerRarity[] rarities = Worker.WorkerRarity.values();
        for (int i = 0; i < rarities.length; i++) {
            cumulative += type.rarityChances[i];
            if (roll < cumulative) return rarities[i];
        }
        return Worker.WorkerRarity.COMMON;
    }

    private static Worker.WorkerRole rollRole(Worker.WorkerRarity rarity) {
        Worker.WorkerRole[] roles = Worker.WorkerRole.values();
        switch (rarity) {
            case LEGENDARY:
                return roles[4 + random.nextInt(3)]; // LEAD, DIRECTOR, CEO
            case EPIC:
                return roles[3 + random.nextInt(3)]; // SENIOR, LEAD, DIRECTOR
            case RARE:
                return roles[2 + random.nextInt(2)]; // MID, SENIOR
            default:
                return roles[random.nextInt(3)]; // INTERN, JUNIOR, MID
        }
    }
}
