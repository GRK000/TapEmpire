package com.example.juego;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Monetization system - handles VIP tiers, starter pack, whale detection,
 * premium currency (gems), and boost packs.
 *
 * Operates as a layer ON TOP of GameState - reads values, applies multipliers,
 * but never blocks gameplay.
 */
public class MonetizationManager implements Serializable {

    // ============================
    // PREMIUM CURRENCY: GEMS
    // ============================
    private int gems = 0;

    // ============================
    // PHASE 1: STARTER PACK
    // ============================
    private boolean starterPackPurchased = false;
    private boolean starterPackExpired = false;
    private long firstSessionTime = 0;          // timestamp of first game open
    private static final long STARTER_PACK_WINDOW_MS = 24 * 60 * 60 * 1000L; // 24h window

    // ============================
    // PHASE 2: VIP SYSTEM
    // ============================
    private int totalSpentCents = 0;  // cumulative spend in cents

    public enum VipTier {
        NONE(0, 1.0, 0, ""),
        BRONZE(399, 1.05, 5, "🥉"),
        SILVER(999, 1.12, 15, "🥈"),
        GOLD(2499, 1.25, 30, "🥇"),
        PLATINUM(4999, 1.50, 50, "💎"),
        DIAMOND(9999, 2.00, 100, "👑");

        public final int thresholdCents;
        public final double productionMultiplier;
        public final int dailyGemBonus;
        public final String badge;

        VipTier(int threshold, double mult, int dailyGems, String badge) {
            this.thresholdCents = threshold;
            this.productionMultiplier = mult;
            this.dailyGemBonus = dailyGems;
            this.badge = badge;
        }
    }

    private long lastDailyGemClaim = 0;

    // ============================
    // PHASE 3: WHALE DETECTION
    // ============================
    private List<Long> purchaseTimestamps = new ArrayList<>();
    private boolean whaleOffersUnlocked = false;
    private boolean exclusiveMultiplierActive = false;
    private long exclusiveMultiplierExpiry = 0;
    private static final double WHALE_EXCLUSIVE_MULTIPLIER = 1.5;

    // ============================
    // BOOST PACKS
    // ============================
    private boolean productionBoostActive = false;
    private long productionBoostExpiry = 0;
    private double productionBoostMultiplier = 1.0;

    private boolean autoTapActive = false;
    private long autoTapExpiry = 0;

    // ============================
    // IAP PRODUCT DEFINITIONS
    // ============================

    /**
     * Represents a purchasable pack/product.
     */
    public static class ShopPack implements Serializable {
        public final String id;
        public final String titleKey;       // string resource key
        public final String descKey;        // string resource key
        public final String emoji;
        public final int priceCents;
        public final PackType type;
        public final int gems;              // gems included
        public final double coinMultiplier; // production boost value (e.g. 2.0 = x2)
        public final long boostDurationMs;  // 0 = permanent
        public final boolean requiresWhale; // only for whale-tier
        public final VipTier minVipTier;    // minimum VIP to see this

        public ShopPack(String id, String titleKey, String descKey, String emoji,
                        int priceCents, PackType type, int gems, double coinMult,
                        long boostDur, boolean whale, VipTier minVip) {
            this.id = id;
            this.titleKey = titleKey;
            this.descKey = descKey;
            this.emoji = emoji;
            this.priceCents = priceCents;
            this.type = type;
            this.gems = gems;
            this.coinMultiplier = coinMult;
            this.boostDurationMs = boostDur;
            this.requiresWhale = whale;
            this.minVipTier = minVip;
        }
    }

    public enum PackType {
        STARTER,        // one-time starter pack
        GEM_PACK,       // gems only
        BOOST_PACK,     // timed boost
        VALUE_PACK,     // gems + boost
        WHALE_EXCLUSIVE // only for detected whales
    }

    // ============================
    // ALL SHOP PACKS
    // ============================
    public static final List<ShopPack> ALL_PACKS = new ArrayList<>();
    static {
        // === PHASE 1: STARTER PACK ===
        ALL_PACKS.add(new ShopPack("starter_pack", "pack_starter_title", "pack_starter_desc",
                "🚀", 399, PackType.STARTER,
                200, 10.0, 24 * 60 * 60 * 1000L, false, VipTier.NONE));

        // === GEM PACKS (escalera progresiva) ===
        ALL_PACKS.add(new ShopPack("gems_small", "pack_gems_small_title", "pack_gems_small_desc",
                "💎", 199, PackType.GEM_PACK,
                100, 0, 0, false, VipTier.NONE));
        ALL_PACKS.add(new ShopPack("gems_medium", "pack_gems_med_title", "pack_gems_med_desc",
                "💎", 499, PackType.GEM_PACK,
                300, 0, 0, false, VipTier.NONE));
        ALL_PACKS.add(new ShopPack("gems_large", "pack_gems_large_title", "pack_gems_large_desc",
                "💎", 999, PackType.GEM_PACK,
                800, 0, 0, false, VipTier.NONE));
        ALL_PACKS.add(new ShopPack("gems_mega", "pack_gems_mega_title", "pack_gems_mega_desc",
                "👑", 1999, PackType.GEM_PACK,
                2000, 0, 0, false, VipTier.NONE));

        // === BOOST PACKS (with increasing VIP requirements) ===
        ALL_PACKS.add(new ShopPack("boost_2x_1h", "pack_boost_2x_title", "pack_boost_2x_desc",
                "⚡", 299, PackType.BOOST_PACK,
                0, 2.0, 60 * 60 * 1000L, false, VipTier.NONE));
        ALL_PACKS.add(new ShopPack("boost_5x_1h", "pack_boost_5x_title", "pack_boost_5x_desc",
                "🔥", 599, PackType.BOOST_PACK,
                0, 5.0, 60 * 60 * 1000L, false, VipTier.BRONZE));
        ALL_PACKS.add(new ShopPack("boost_10x_30m", "pack_boost_10x_title", "pack_boost_10x_desc",
                "💥", 999, PackType.BOOST_PACK,
                0, 10.0, 30 * 60 * 1000L, false, VipTier.SILVER));

        // === VALUE PACKS ===
        ALL_PACKS.add(new ShopPack("value_silver", "pack_value_silver_title", "pack_value_silver_desc",
                "🎁", 799, PackType.VALUE_PACK,
                400, 3.0, 2 * 60 * 60 * 1000L, false, VipTier.NONE));
        ALL_PACKS.add(new ShopPack("value_gold", "pack_value_gold_title", "pack_value_gold_desc",
                "🎉", 1499, PackType.VALUE_PACK,
                1000, 5.0, 4 * 60 * 60 * 1000L, false, VipTier.BRONZE));

        // === PHASE 3: WHALE EXCLUSIVES ===
        ALL_PACKS.add(new ShopPack("whale_ultra", "pack_whale_ultra_title", "pack_whale_ultra_desc",
                "🐋", 4999, PackType.WHALE_EXCLUSIVE,
                5000, 15.0, 12 * 60 * 60 * 1000L, true, VipTier.GOLD));
        ALL_PACKS.add(new ShopPack("whale_omega", "pack_whale_omega_title", "pack_whale_omega_desc",
                "🌟", 9999, PackType.WHALE_EXCLUSIVE,
                15000, 0, 0, true, VipTier.GOLD));
    }

    // ============================
    // CORE METHODS
    // ============================

    public void initFirstSession() {
        if (firstSessionTime == 0) {
            firstSessionTime = System.currentTimeMillis();
        }
    }

    /**
     * Process a purchase. Called after Google Play confirms.
     * Returns true on success.
     */
    public boolean processPurchase(String packId, GameState state) {
        ShopPack pack = findPack(packId);
        if (pack == null) return false;
        if (pack.type == PackType.STARTER && starterPackPurchased) return false;

        // Record spend
        totalSpentCents += pack.priceCents;
        purchaseTimestamps.add(System.currentTimeMillis());

        // Grant gems
        if (pack.gems > 0) {
            gems += pack.gems;
        }

        // Apply boost
        if (pack.coinMultiplier > 1.0 && pack.boostDurationMs > 0) {
            productionBoostActive = true;
            productionBoostMultiplier = pack.coinMultiplier;
            productionBoostExpiry = System.currentTimeMillis() + pack.boostDurationMs;
        }

        // Starter pack: also give flat coins (10x current production * 3600)
        if (pack.type == PackType.STARTER) {
            starterPackPurchased = true;
            double bonus = Math.max(1000, state.getProductionPerSecond() * 3600 * 10);
            state.setCoins(state.getCoins() + bonus);
            state.setTotalCoinsEarned(state.getTotalCoinsEarned() + bonus);
        }

        // Whale exclusive: omega grants permanent VIP-tier upgrade (handled via totalSpentCents -> getVipTier())

        // Check whale status
        checkWhaleStatus();

        return true;
    }

    /**
     * Buy something with gems (gem-only store).
     */
    public boolean spendGems(int amount) {
        if (gems >= amount) {
            gems -= amount;
            return true;
        }
        return false;
    }

    // ============================
    // PHASE 1: STARTER PACK
    // ============================

    public boolean isStarterPackAvailable() {
        if (starterPackPurchased || starterPackExpired) return false;
        if (firstSessionTime == 0) return false;
        long elapsed = System.currentTimeMillis() - firstSessionTime;
        if (elapsed > STARTER_PACK_WINDOW_MS) {
            starterPackExpired = true;
            return false;
        }
        return true;
    }

    /** Returns remaining ms for starter pack offer, or 0 */
    public long getStarterPackRemainingMs() {
        if (!isStarterPackAvailable()) return 0;
        long remaining = STARTER_PACK_WINDOW_MS - (System.currentTimeMillis() - firstSessionTime);
        return Math.max(0, remaining);
    }

    // ============================
    // PHASE 2: VIP SYSTEM
    // ============================

    public VipTier getVipTier() {
        VipTier best = VipTier.NONE;
        for (VipTier tier : VipTier.values()) {
            if (totalSpentCents >= tier.thresholdCents) {
                best = tier;
            }
        }
        return best;
    }

    public int getVipProgress() {
        VipTier current = getVipTier();
        VipTier[] tiers = VipTier.values();
        int nextIdx = current.ordinal() + 1;
        if (nextIdx >= tiers.length) return 100;
        VipTier next = tiers[nextIdx];
        int range = next.thresholdCents - current.thresholdCents;
        int progress = totalSpentCents - current.thresholdCents;
        return Math.min(100, (int)((progress / (double)range) * 100));
    }

    public VipTier getNextVipTier() {
        VipTier current = getVipTier();
        VipTier[] tiers = VipTier.values();
        int nextIdx = current.ordinal() + 1;
        if (nextIdx >= tiers.length) return null;
        return tiers[nextIdx];
    }

    public int getCentsToNextTier() {
        VipTier next = getNextVipTier();
        if (next == null) return 0;
        return Math.max(0, next.thresholdCents - totalSpentCents);
    }

    public boolean canClaimDailyGems() {
        VipTier tier = getVipTier();
        if (tier == VipTier.NONE) return false;
        long dayMs = 24 * 60 * 60 * 1000L;
        return System.currentTimeMillis() - lastDailyGemClaim > dayMs;
    }

    public int claimDailyGems() {
        if (!canClaimDailyGems()) return 0;
        VipTier tier = getVipTier();
        lastDailyGemClaim = System.currentTimeMillis();
        gems += tier.dailyGemBonus;
        return tier.dailyGemBonus;
    }

    /**
     * Returns the VIP production multiplier (applied on top of everything).
     */
    public double getVipProductionMultiplier() {
        return getVipTier().productionMultiplier;
    }

    // ============================
    // PHASE 3: WHALE DETECTION
    // ============================

    private void checkWhaleStatus() {
        if (whaleOffersUnlocked) return;
        long cutoff = System.currentTimeMillis() - 86_400_000L; // 24h
        long recentCount = 0;
        for (Long ts : purchaseTimestamps) {
            if (ts > cutoff) recentCount++;
        }
        if (recentCount >= 3) {
            whaleOffersUnlocked = true;
        }
    }

    public boolean isWhaleOffersUnlocked() {
        return whaleOffersUnlocked;
    }

    public boolean activateExclusiveMultiplier() {
        if (!whaleOffersUnlocked) return false;
        if (!spendGems(500)) return false;
        exclusiveMultiplierActive = true;
        exclusiveMultiplierExpiry = System.currentTimeMillis() + 6 * 60 * 60 * 1000L; // 6h
        return true;
    }

    // ============================
    // BOOST SYSTEM
    // ============================

    /** Total production multiplier from all monetization sources */
    public double getTotalProductionMultiplier() {
        double mult = getVipProductionMultiplier();

        // Active boost
        if (productionBoostActive && System.currentTimeMillis() < productionBoostExpiry) {
            mult *= productionBoostMultiplier;
        } else {
            productionBoostActive = false;
        }

        // Whale exclusive multiplier
        if (exclusiveMultiplierActive && System.currentTimeMillis() < exclusiveMultiplierExpiry) {
            mult *= WHALE_EXCLUSIVE_MULTIPLIER;
        } else {
            exclusiveMultiplierActive = false;
        }

        return mult;
    }

    public boolean isBoostActive() {
        if (productionBoostActive && System.currentTimeMillis() >= productionBoostExpiry) {
            productionBoostActive = false;
        }
        return productionBoostActive;
    }

    public long getBoostRemainingMs() {
        if (!isBoostActive()) return 0;
        return Math.max(0, productionBoostExpiry - System.currentTimeMillis());
    }

    public double getBoostMultiplier() {
        return isBoostActive() ? productionBoostMultiplier : 1.0;
    }

    public boolean isAutoTapActive() {
        if (autoTapActive && System.currentTimeMillis() >= autoTapExpiry) {
            autoTapActive = false;
        }
        return autoTapActive;
    }

    /** Buy auto-tap with gems (1h) */
    public boolean buyAutoTap() {
        if (!spendGems(50)) return false;
        autoTapActive = true;
        autoTapExpiry = System.currentTimeMillis() + 60 * 60 * 1000L;
        return true;
    }

    /** Buy instant coins with gems */
    public boolean buyInstantCoins(GameState state) {
        if (!spendGems(100)) return false;
        double bonus = Math.max(10000, state.getProductionPerSecond() * 3600);
        state.setCoins(state.getCoins() + bonus);
        state.setTotalCoinsEarned(state.getTotalCoinsEarned() + bonus);
        return true;
    }

    // ============================
    // AVAILABLE PACKS (filtered)
    // ============================

    /** Get packs visible to this player */
    public List<ShopPack> getAvailablePacks() {
        VipTier currentTier = getVipTier();
        List<ShopPack> available = new ArrayList<>();
        for (ShopPack pack : ALL_PACKS) {
            // Filter starter pack
            if (pack.type == PackType.STARTER) {
                if (isStarterPackAvailable()) available.add(pack);
                continue;
            }
            // Filter whale exclusives
            if (pack.requiresWhale && !whaleOffersUnlocked) continue;
            // Filter by VIP tier
            if (currentTier.ordinal() < pack.minVipTier.ordinal()) continue;
            available.add(pack);
        }
        return available;
    }

    // ============================
    // UTILITIES
    // ============================

    private ShopPack findPack(String id) {
        for (ShopPack pack : ALL_PACKS) {
            if (pack.id.equals(id)) return pack;
        }
        return null;
    }

    public static String formatPrice(int cents) {
        return String.format(java.util.Locale.US, "$%d.%02d", cents / 100, cents % 100);
    }

    // ============================
    // GETTERS/SETTERS for persistence
    // ============================

    public int getGems() { return gems; }
    public void setGems(int g) { this.gems = g; }
    public boolean isStarterPackPurchased() { return starterPackPurchased; }
    public void setStarterPackPurchased(boolean b) { this.starterPackPurchased = b; }
    public boolean isStarterPackExpired() { return starterPackExpired; }
    public void setStarterPackExpired(boolean b) { this.starterPackExpired = b; }
    public long getFirstSessionTime() { return firstSessionTime; }
    public void setFirstSessionTime(long t) { this.firstSessionTime = t; }
    public int getTotalSpentCents() { return totalSpentCents; }
    public void setTotalSpentCents(int c) { this.totalSpentCents = c; }
    public long getLastDailyGemClaim() { return lastDailyGemClaim; }
    public void setLastDailyGemClaim(long t) { this.lastDailyGemClaim = t; }
    public boolean isWhaleUnlocked() { return whaleOffersUnlocked; }
    public void setWhaleUnlocked(boolean b) { this.whaleOffersUnlocked = b; }
    public boolean isExclusiveMultiplierActive() { return exclusiveMultiplierActive; }
    public void setExclusiveMultiplierActive(boolean b) { this.exclusiveMultiplierActive = b; }
    public long getExclusiveMultiplierExpiry() { return exclusiveMultiplierExpiry; }
    public void setExclusiveMultiplierExpiry(long t) { this.exclusiveMultiplierExpiry = t; }
    public boolean isProductionBoostActive() { return productionBoostActive; }
    public void setProductionBoostActive(boolean b) { this.productionBoostActive = b; }
    public long getProductionBoostExpiry() { return productionBoostExpiry; }
    public void setProductionBoostExpiry(long t) { this.productionBoostExpiry = t; }
    public double getProductionBoostMult() { return productionBoostMultiplier; }
    public void setProductionBoostMult(double m) { this.productionBoostMultiplier = m; }
    public boolean isAutoTapActiveRaw() { return autoTapActive; }
    public void setAutoTapActive(boolean b) { this.autoTapActive = b; }
    public long getAutoTapExpiry() { return autoTapExpiry; }
    public void setAutoTapExpiry(long t) { this.autoTapExpiry = t; }
    public List<Long> getPurchaseTimestamps() { return purchaseTimestamps; }
    public void setPurchaseTimestamps(List<Long> ts) { this.purchaseTimestamps = ts; }
}
