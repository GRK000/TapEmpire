package com.example.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests del sistema genético: la cría debe heredar el linaje de ambos padres.
 */
public class PetBreedingTest {

    private Pet ownedPet(Pet.PetType type) {
        Pet pet = new Pet(type);
        pet.setOwned(true);
        return pet;
    }

    @Test
    public void breed_creaHibridoIncubando() {
        Pet p1 = ownedPet(Pet.PetType.ROBO_CAT);
        Pet p2 = ownedPet(Pet.PetType.HOLO_DRAGON);

        Pet offspring = Pet.breed(p1, p2);

        assertNotNull(offspring);
        assertTrue(offspring.isHybrid());
        assertTrue(offspring.isIncubating());
        assertTrue(offspring.isOwned());
        // El tipo dominante es el de mayor bonus (HOLO_DRAGON 0.08 > ROBO_CAT 0.05)
        assertEquals(Pet.PetType.HOLO_DRAGON, offspring.getType());
    }

    @Test
    public void breed_registraElLinaje() {
        Pet p1 = ownedPet(Pet.PetType.CYBER_DOG);
        Pet p2 = ownedPet(Pet.PetType.QUANTUM_BIRD);

        Pet offspring = Pet.breed(p1, p2);

        assertEquals(p1.getPetId(), offspring.getParentId1());
        assertEquals(p2.getPetId(), offspring.getParentId2());
    }

    @Test
    public void breed_incrementaLaGeneracion() {
        Pet p1 = ownedPet(Pet.PetType.ROBO_CAT);
        Pet p2 = ownedPet(Pet.PetType.CYBER_DOG);
        p1.setGeneration(3);
        p2.setGeneration(1);

        Pet offspring = Pet.breed(p1, p2);

        assertEquals(4, offspring.getGeneration());
    }

    @Test
    public void breed_heredaComoMaximoDosRasgos() {
        Pet p1 = ownedPet(Pet.PetType.ROBO_CAT);
        Pet p2 = ownedPet(Pet.PetType.CYBER_DOG);

        Pet offspring = Pet.breed(p1, p2);

        assertNotNull(offspring.getTraits());
        assertTrue(offspring.getTraits().size() <= 2);
    }

    @Test
    public void breed_poneCooldownALosPadres() {
        Pet p1 = ownedPet(Pet.PetType.ROBO_CAT);
        Pet p2 = ownedPet(Pet.PetType.CYBER_DOG);

        Pet.breed(p1, p2);

        assertTrue(p1.getLastBreedTime() > 0);
        assertTrue(p2.getLastBreedTime() > 0);
    }
}
