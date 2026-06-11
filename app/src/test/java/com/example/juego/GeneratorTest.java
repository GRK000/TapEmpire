package com.example.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests de la economía de generadores: hitos, costes y compra masiva.
 */
public class GeneratorTest {

    private Generator newGenerator() {
        return new Generator("test_gen", "Test", "desc", "🏭", 1.0, 100.0, 1.15, 0);
    }

    @Test
    public void milestoneBonus_duplicaEnCadaHito() {
        Generator gen = newGenerator();
        assertEquals(1.0, gen.getMilestoneBonus(), 0.0001);

        gen.setOwned(9);
        assertEquals(1.0, gen.getMilestoneBonus(), 0.0001);

        gen.setOwned(10);
        assertEquals(2.0, gen.getMilestoneBonus(), 0.0001);

        gen.setOwned(25);
        assertEquals(4.0, gen.getMilestoneBonus(), 0.0001);

        gen.setOwned(100);
        assertEquals(16.0, gen.getMilestoneBonus(), 0.0001); // 10, 25, 50, 100

        gen.setOwned(1000);
        assertEquals(1024.0, gen.getMilestoneBonus(), 0.0001); // los 10 hitos
    }

    @Test
    public void nextMilestone_avanzaYTermina() {
        Generator gen = newGenerator();
        assertEquals(10, gen.getNextMilestone());

        gen.setOwned(10);
        assertEquals(25, gen.getNextMilestone());

        gen.setOwned(1000);
        assertEquals(-1, gen.getNextMilestone()); // todos completados
    }

    @Test
    public void milestoneBonus_afectaALaProduccion() {
        Generator gen = newGenerator();
        gen.setOwned(9);
        double before = gen.getProductionPerSecond();
        gen.setOwned(10);
        double after = gen.getProductionPerSecond();
        // 10/9 por la unidad extra, x2 por el hito
        assertEquals(before / 9 * 10 * 2, after, 0.0001);
    }

    @Test
    public void costForAmount_esLaSumaDeCostesIndividuales() {
        Generator gen = newGenerator();
        gen.setOwned(5);
        double expected = 0;
        for (int i = 0; i < 10; i++) {
            expected += 100.0 * Math.pow(1.15, 5 + i);
        }
        assertEquals(expected, gen.getCostForAmount(10), 0.001);
    }

    @Test
    public void maxAffordable_coherenteConElCoste() {
        Generator gen = newGenerator();
        double budget = 1000.0;
        int max = gen.getMaxAffordable(budget);
        assertTrue(max > 0);
        // Lo que dice que puedes comprar, cabe en el presupuesto…
        assertTrue(gen.getCostForAmount(max) <= budget);
        // …y una unidad más ya no cabe
        assertFalse(gen.getCostForAmount(max + 1) <= budget);
    }

    @Test
    public void maxAffordable_sinDineroEsCero() {
        Generator gen = newGenerator();
        assertEquals(0, gen.getMaxAffordable(0.0));
        assertEquals(0, gen.getMaxAffordable(99.9));
    }
}
