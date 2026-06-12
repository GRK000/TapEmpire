package com.example.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Tests de la economía central: Legado de Aurora, Pacto, prestigio y formato.
 * GameState es un singleton, así que cada test fija explícitamente el estado
 * que necesita y los logros se marcan como desbloqueados para que sus
 * recompensas no contaminen las cuentas.
 */
public class GameStateEconomyTest {

    private GameState state;

    @Before
    public void setUp() {
        Locale.setDefault(Locale.US);
        state = GameState.getInstance();
        // Evitar recompensas de logros durante los tests
        for (Achievement ach : state.getAchievements()) {
            ach.setUnlocked(true);
        }
    }

    // ==================== LEGADO DE AURORA ====================

    @Test
    public void legado_efectosEscalanPorNivel() {
        state.setLegacyLevel(GameState.LEGACY_ECHO, 0);
        assertEquals(1.0, state.getLegacyTapMultiplier(), 0.0001);
        state.setLegacyLevel(GameState.LEGACY_ECHO, 4);
        assertEquals(2.0, state.getLegacyTapMultiplier(), 0.0001);

        state.setLegacyLevel(GameState.LEGACY_PACT, 0);
        assertEquals(8, state.getLegacyOfflineCapHours());
        state.setLegacyLevel(GameState.LEGACY_PACT, 8);
        assertEquals(24, state.getLegacyOfflineCapHours());

        state.setLegacyLevel(GameState.LEGACY_SEED, 0);
        assertEquals(0.0, state.getLegacyStartingCoins(), 0.0001);
        state.setLegacyLevel(GameState.LEGACY_SEED, 3);
        assertEquals(100_000.0, state.getLegacyStartingCoins(), 0.0001);
    }

    @Test
    public void legado_compraDescuentaRenombre() {
        state.setLegacyLevel(GameState.LEGACY_ECHO, 0);
        state.setPrestigePoints(10.0);

        double cost = state.getLegacyCost(GameState.LEGACY_ECHO);
        assertTrue(state.buyLegacyUpgrade(GameState.LEGACY_ECHO));

        assertEquals(1, state.getLegacyLevel(GameState.LEGACY_ECHO));
        assertEquals(10.0 - cost, state.getPrestigePoints(), 0.0001);
    }

    @Test
    public void legado_sinRenombreNoCompra() {
        state.setLegacyLevel(GameState.LEGACY_MAGNET, 0);
        state.setPrestigePoints(0.0);
        assertFalse(state.buyLegacyUpgrade(GameState.LEGACY_MAGNET));
        assertEquals(0, state.getLegacyLevel(GameState.LEGACY_MAGNET));
    }

    @Test
    public void legado_respetaNivelMaximo() {
        int max = GameState.getLegacyMaxLevel(GameState.LEGACY_MAGNET);
        state.setLegacyLevel(GameState.LEGACY_MAGNET, max);
        state.setPrestigePoints(1_000_000.0);
        assertFalse(state.buyLegacyUpgrade(GameState.LEGACY_MAGNET));
    }

    @Test
    public void legado_costeCreceExponencialmente() {
        state.setLegacyLevel(GameState.LEGACY_NETWORK, 0);
        double cost0 = state.getLegacyCost(GameState.LEGACY_NETWORK);
        state.setLegacyLevel(GameState.LEGACY_NETWORK, 5);
        double cost5 = state.getLegacyCost(GameState.LEGACY_NETWORK);
        assertTrue(cost5 > cost0 * 10);
        // Limpieza
        state.setLegacyLevel(GameState.LEGACY_NETWORK, 0);
    }

    // ==================== EL CIERRE DEL PACTO ====================

    @Test
    public void pacto_requiereNexusYValoracion() {
        state.setPactWon(false);
        state.getWorlds().get(9).setUnlocked(false);
        state.setTotalCoinsEarned(GameState.VEX_VALUATION * 2);
        assertFalse(state.canClosePact()); // sin Nexus Prime no hay duelo

        state.getWorlds().get(9).setUnlocked(true);
        assertTrue(state.canClosePact());

        state.setTotalCoinsEarned(GameState.VEX_VALUATION / 2);
        assertFalse(state.canClosePact()); // sin superar la valoración tampoco

        // Limpieza
        state.getWorlds().get(9).setUnlocked(false);
        state.setTotalCoinsEarned(0);
    }

    @Test
    public void pacto_ganarActivaElSello() {
        state.setPactWon(false);
        assertEquals(1.0, state.getPactSealMultiplier(), 0.0001);

        state.getWorlds().get(9).setUnlocked(true);
        state.setTotalCoinsEarned(GameState.VEX_VALUATION);
        assertTrue(state.closePact());

        assertTrue(state.isPactWon());
        assertEquals(2.0, state.getPactSealMultiplier(), 0.0001);
        // Una vez ganado, no se puede volver a cerrar
        assertFalse(state.canClosePact());

        // Limpieza
        state.setPactWon(false);
        state.getWorlds().get(9).setUnlocked(false);
        state.setTotalCoinsEarned(0);
    }

    // ==================== PRESTIGIO ====================

    @Test
    public void prestigio_otorgaPuntosYReinicia() {
        state.setPrestigeLevel(0);
        state.setPrestigePoints(0.0);
        state.setLegacyLevel(GameState.LEGACY_SEED, 0);
        state.setTotalCoinsEarned(4_000_000.0); // sqrt(4) = 2 puntos
        state.setCoins(999.0);

        assertTrue(state.canPrestige());
        state.performPrestige();

        assertEquals(1, state.getPrestigeLevel());
        assertEquals(2.0, state.getPrestigePoints(), 0.0001);
        assertEquals(0.0, state.getCoins(), 0.0001); // sin Capital Semilla
        assertEquals(0.0, state.getTotalCoinsEarned(), 0.0001);

        // Limpieza
        state.setPrestigeLevel(0);
        state.setPrestigePoints(0.0);
    }

    @Test
    public void prestigio_capitalSemillaDaArranque() {
        state.setPrestigeLevel(0);
        state.setLegacyLevel(GameState.LEGACY_SEED, 2); // 10K de arranque
        state.setTotalCoinsEarned(1_000_000.0);

        state.performPrestige();

        assertEquals(10_000.0, state.getCoins(), 0.0001);

        // Limpieza
        state.setLegacyLevel(GameState.LEGACY_SEED, 0);
        state.setPrestigeLevel(0);
        state.setPrestigePoints(0.0);
        state.setCoins(0.0);
    }

    // ==================== COMPRA DE GENERADORES ====================

    @Test
    public void buyGenerators_compraHastaElLimiteDelDinero() {
        Generator gen = state.getGenerators().get(0);
        int ownedBefore = gen.getOwned();
        double cost3 = gen.getCostForAmount(3);
        state.setCoins(cost3 + 0.5);

        int bought = state.buyGenerators(0, 10); // pide 10, solo caben 3

        assertEquals(3, bought);
        assertEquals(ownedBefore + 3, gen.getOwned());
        assertTrue(state.getCoins() < 1.0);
    }

    // ==================== FORMATO DE NÚMEROS ====================

    @Test
    public void fmt_formateaEscalas() {
        assertEquals("999", GameState.fmt(999));
        assertEquals("1.5K", GameState.fmt(1_500));
        assertEquals("2.50M", GameState.fmt(2_500_000));
        assertEquals("1.00B", GameState.fmt(1_000_000_000));
        assertEquals("1.00T", GameState.fmt(1_000_000_000_000.0));
        assertEquals("1.00Qa", GameState.fmt(1_000_000_000_000_000.0));
    }
}
