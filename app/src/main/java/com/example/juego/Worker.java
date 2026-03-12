package com.example.juego;

import java.io.Serializable;
import java.util.UUID;

/**
 * Sistema de Trabajadores — Entidad completa con clase, rareza, estadísticas y contrato
 */
public class Worker implements Serializable {

    // === ENUMS ===

    public enum WorkerClass {
        SCIENTIST("🔬", 1.3),
        LABORER("🔧", 1.0),
        EXECUTIVE("💼", 1.5),
        SECURITY("🛡️", 0.9),
        ENGINEER("⚙️", 1.2),
        MANAGER("📋", 1.4);

        public final String emoji;
        public final double salaryMultiplier;

        WorkerClass(String emoji, double salaryMultiplier) {
            this.emoji = emoji;
            this.salaryMultiplier = salaryMultiplier;
        }
    }

    public enum WorkerRarity {
        COMMON("⬜", 0x9E9E9E, 20, 50, 1.0),
        RARE("🟦", 0x42A5F5, 40, 70, 1.5),
        EPIC("🟪", 0xAB47BC, 60, 85, 2.5),
        LEGENDARY("🟧", 0xFFA726, 80, 100, 4.0);

        public final String icon;
        public final int color;
        public final int minProductivity;
        public final int maxProductivity;
        public final double salaryMultiplier;

        WorkerRarity(String icon, int color, int minProd, int maxProd, double salaryMult) {
            this.icon = icon;
            this.color = color;
            this.minProductivity = minProd;
            this.maxProductivity = maxProd;
            this.salaryMultiplier = salaryMult;
        }
    }

    public enum WorkerMood {
        HAPPY("😄", 1.2),
        NEUTRAL("😐", 1.0),
        ANGRY("😠", 0.6),
        ON_STRIKE("✊", 0.0);

        public final String emoji;
        public final double productivityMultiplier;

        WorkerMood(String emoji, double mult) {
            this.emoji = emoji;
            this.productivityMultiplier = mult;
        }
    }

    public enum WorkerRole {
        INTERN(0.5, 0),
        JUNIOR(0.8, 1),
        MID(1.0, 3),
        SENIOR(1.3, 6),
        LEAD(1.6, 10),
        DIRECTOR(2.0, 15),
        CEO(3.0, 20);

        public final double salaryMultiplier;
        public final int minExperience;

        WorkerRole(double salaryMult, int minExp) {
            this.salaryMultiplier = salaryMult;
            this.minExperience = minExp;
        }
    }

    // === FIELDS ===

    private String id;
    private String name;
    private WorkerClass workerClass;
    private WorkerRarity rarity;
    private WorkerRole role;
    private int yearsExperience;
    private double baseSalary;
    private int productivity; // 0-100
    private WorkerMood mood;
    private String maritalStatus; // "Single", "Married", "Divorced"
    private int numberOfChildren;
    private String assignedGeneratorId; // null if unassigned
    private int contractDurationMonths;
    private boolean strikeRights;
    private int unpaidTicks; // consecutive unpaid salary ticks

    // Education
    private String education; // e.g. "Mechanical Engineering", "MBA", "Vocational: Welding", "None"

    // Contract details
    private double contractSalary;      // negotiated salary
    private String contractSchedule;    // "full_time", "part_time", "flexible"
    private String contractCategory;    // "standard", "premium", "executive"
    private boolean contractSigned;     // has the worker accepted a contract?

    /**
     * Inner class representing a contract template that the player creates
     */
    public static class ContractTemplate implements Serializable {
        public String id;
        public String name;
        public double salary;
        public String schedule;   // "full_time", "part_time", "flexible"
        public String category;   // "standard", "premium", "executive"
        public boolean strikeRights;
        public int durationMonths;

        public ContractTemplate(String id, String name, double salary, String schedule,
                                String category, boolean strikeRights, int durationMonths) {
            this.id = id;
            this.name = name;
            this.salary = salary;
            this.schedule = schedule;
            this.category = category;
            this.strikeRights = strikeRights;
            this.durationMonths = durationMonths;
        }

        /** Schedule multiplier: part_time=0.6x prod/0.5x salary, flexible=0.9x prod/0.85x salary */
        public double getScheduleProductivityMult() {
            switch (schedule) {
                case "part_time": return 0.6;
                case "flexible": return 0.9;
                default: return 1.0;
            }
        }

        public double getScheduleSalaryMult() {
            switch (schedule) {
                case "part_time": return 0.5;
                case "flexible": return 0.85;
                default: return 1.0;
            }
        }
    }

    /**
     * Check if a worker would accept a contract offer.
     * Workers evaluate: salary vs expectations, schedule, strike rights.
     * Returns acceptance probability 0.0-1.0
     */
    public double evaluateContract(ContractTemplate contract) {
        double expectedSalary = getMonthlySalary(); // what they think they deserve
        double offerRatio = contract.salary / Math.max(1, expectedSalary);

        double score = 0;

        // Salary evaluation (most important: 50% weight)
        if (offerRatio >= 1.2) score += 50;
        else if (offerRatio >= 1.0) score += 40;
        else if (offerRatio >= 0.8) score += 20;
        else if (offerRatio >= 0.6) score += 5;
        else score += 0; // too low, almost guaranteed rejection

        // Schedule preference (20% weight) - higher rarity prefers flexible
        switch (contract.schedule) {
            case "flexible":
                score += (rarity.ordinal() >= 2) ? 20 : 15;
                break;
            case "full_time":
                score += (rarity.ordinal() <= 1) ? 18 : 12;
                break;
            case "part_time":
                score += 10;
                break;
        }

        // Strike rights (15% weight) - workers strongly prefer having them
        if (contract.strikeRights) score += 15;
        else score += (rarity == WorkerRarity.LEGENDARY) ? 12 : 3;

        // Category match (15% weight)
        if (contract.category.equals("executive") && rarity.ordinal() >= 2) score += 15;
        else if (contract.category.equals("premium") && rarity.ordinal() >= 1) score += 13;
        else if (contract.category.equals("standard")) score += 10;
        else score += 5;

        return Math.max(0, Math.min(1.0, score / 100.0));
    }

    /** Apply a signed contract to this worker */
    public void applyContract(ContractTemplate contract) {
        this.contractSalary = contract.salary;
        this.contractSchedule = contract.schedule;
        this.contractCategory = contract.category;
        this.strikeRights = contract.strikeRights;
        this.contractDurationMonths = contract.durationMonths;
        this.contractSigned = true;
    }

    /** Resolve a strike by offering new contract terms */
    public boolean resolveStrike(ContractTemplate newContract) {
        if (mood != WorkerMood.ON_STRIKE) return false;
        double acceptance = evaluateContract(newContract);
        // Strikers demand better: need >0.7 acceptance
        if (acceptance >= 0.7) {
            applyContract(newContract);
            mood = WorkerMood.NEUTRAL;
            unpaidTicks = 0;
            return true;
        }
        return false;
    }

    // === CONSTRUCTORS ===

    public Worker(String name, WorkerClass workerClass, WorkerRarity rarity,
                  WorkerRole role, int yearsExperience, int productivity, double baseSalary) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.workerClass = workerClass;
        this.rarity = rarity;
        this.role = role;
        this.yearsExperience = yearsExperience;
        this.productivity = Math.max(0, Math.min(100, productivity));
        this.baseSalary = baseSalary;
        this.mood = WorkerMood.HAPPY;
        this.maritalStatus = "Single";
        this.numberOfChildren = 0;
        this.assignedGeneratorId = null;
        this.contractDurationMonths = 12;
        this.strikeRights = true;
        this.unpaidTicks = 0;
        this.contractSalary = 0;
        this.contractSchedule = "full_time";
        this.contractCategory = "standard";
        this.contractSigned = false;
        this.education = "None";
    }

    // Full constructor for deserialization
    public Worker(String id, String name, WorkerClass workerClass, WorkerRarity rarity,
                  WorkerRole role, int yearsExperience, int productivity, double baseSalary,
                  WorkerMood mood, String maritalStatus, int numberOfChildren,
                  String assignedGeneratorId, int contractDurationMonths,
                  boolean strikeRights, int unpaidTicks,
                  double contractSalary, String contractSchedule,
                  String contractCategory, boolean contractSigned,
                  String education) {
        this.id = id;
        this.name = name;
        this.workerClass = workerClass;
        this.rarity = rarity;
        this.role = role;
        this.yearsExperience = yearsExperience;
        this.productivity = productivity;
        this.baseSalary = baseSalary;
        this.mood = mood;
        this.maritalStatus = maritalStatus;
        this.numberOfChildren = numberOfChildren;
        this.assignedGeneratorId = assignedGeneratorId;
        this.contractDurationMonths = contractDurationMonths;
        this.strikeRights = strikeRights;
        this.unpaidTicks = unpaidTicks;
        this.contractSalary = contractSalary;
        this.contractSchedule = contractSchedule != null ? contractSchedule : "full_time";
        this.contractCategory = contractCategory != null ? contractCategory : "standard";
        this.contractSigned = contractSigned;
        this.education = education != null ? education : "None";
    }

    // === COMPUTED ===

    /** Effective productivity (0.0 - 1.0+) considering mood and schedule */
    public double getEffectiveProductivity() {
        double base = (productivity / 100.0) * mood.productivityMultiplier;
        if (contractSigned && contractSchedule != null) {
            switch (contractSchedule) {
                case "part_time": base *= 0.6; break;
                case "flexible": base *= 0.9; break;
            }
        }
        return base;
    }

    /** Monthly salary - uses contract salary if signed, else base calculation */
    public double getMonthlySalary() {
        if (contractSigned && contractSalary > 0) {
            double scheduleMult = 1.0;
            if (contractSchedule != null) {
                switch (contractSchedule) {
                    case "part_time": scheduleMult = 0.5; break;
                    case "flexible": scheduleMult = 0.85; break;
                }
            }
            return contractSalary * scheduleMult;
        }
        return baseSalary * workerClass.salaryMultiplier
                * rarity.salaryMultiplier * role.salaryMultiplier;
    }

    /** Process mood based on whether salary was paid */
    public void tickMood(boolean paid) {
        if (paid) {
            unpaidTicks = 0;
            if (mood == WorkerMood.ANGRY) mood = WorkerMood.NEUTRAL;
            else if (mood == WorkerMood.NEUTRAL) mood = WorkerMood.HAPPY;
            else if (mood == WorkerMood.ON_STRIKE && !strikeRights) mood = WorkerMood.ANGRY;
        } else {
            unpaidTicks++;
            if (unpaidTicks >= 3) mood = WorkerMood.ON_STRIKE;
            else if (unpaidTicks >= 2) mood = WorkerMood.ANGRY;
            else if (mood == WorkerMood.HAPPY) mood = WorkerMood.NEUTRAL;
        }
    }

    /** Whether this worker should be fired (auto-quit after 5 unpaid ticks) */
    public boolean shouldQuit() {
        return unpaidTicks >= 5;
    }

    /** Can this worker be promoted to the next role? */
    public boolean canPromote() {
        int nextOrdinal = role.ordinal() + 1;
        if (nextOrdinal >= WorkerRole.values().length) return false;
        WorkerRole nextRole = WorkerRole.values()[nextOrdinal];
        return yearsExperience >= nextRole.minExperience;
    }

    /** Promote worker to next role */
    public boolean promote() {
        if (!canPromote()) return false;
        role = WorkerRole.values()[role.ordinal() + 1];
        return true;
    }

    // === GETTERS ===
    public String getId() { return id; }
    public String getName() { return name; }
    public WorkerClass getWorkerClass() { return workerClass; }
    public WorkerRarity getRarity() { return rarity; }
    public WorkerRole getRole() { return role; }
    public int getYearsExperience() { return yearsExperience; }
    public double getBaseSalary() { return baseSalary; }
    public int getProductivity() { return productivity; }
    public WorkerMood getMood() { return mood; }
    public String getMaritalStatus() { return maritalStatus; }
    public int getNumberOfChildren() { return numberOfChildren; }
    public String getAssignedGeneratorId() { return assignedGeneratorId; }
    public int getContractDurationMonths() { return contractDurationMonths; }
    public boolean hasStrikeRights() { return strikeRights; }
    public int getUnpaidTicks() { return unpaidTicks; }
    public boolean isAssigned() { return assignedGeneratorId != null; }

    // === SETTERS ===
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setWorkerClass(WorkerClass wc) { this.workerClass = wc; }
    public void setRarity(WorkerRarity r) { this.rarity = r; }
    public void setRole(WorkerRole r) { this.role = r; }
    public void setYearsExperience(int y) { this.yearsExperience = y; }
    public void setBaseSalary(double s) { this.baseSalary = s; }
    public void setProductivity(int p) { this.productivity = p; }
    public void setMood(WorkerMood m) { this.mood = m; }
    public void setMaritalStatus(String s) { this.maritalStatus = s; }
    public void setNumberOfChildren(int n) { this.numberOfChildren = n; }
    public void setAssignedGeneratorId(String id) { this.assignedGeneratorId = id; }
    public void setContractDurationMonths(int d) { this.contractDurationMonths = d; }
    public void setStrikeRights(boolean s) { this.strikeRights = s; }
    public void setUnpaidTicks(int t) { this.unpaidTicks = t; }
    public double getContractSalary() { return contractSalary; }
    public void setContractSalary(double s) { this.contractSalary = s; }
    public String getContractSchedule() { return contractSchedule; }
    public void setContractSchedule(String s) { this.contractSchedule = s; }
    public String getContractCategory() { return contractCategory; }
    public void setContractCategory(String c) { this.contractCategory = c; }
    public boolean isContractSigned() { return contractSigned; }
    public void setContractSigned(boolean s) { this.contractSigned = s; }
    public String getEducation() { return education; }
    public void setEducation(String e) { this.education = e; }
}
