package com.example.juego.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.juego.Achievement
import com.example.juego.Pet
import com.example.juego.R

/**
 * Capa de localización para textos que viven en los modelos Java
 * (logros y estados de ánimo de mascotas). El texto del modelo queda
 * como reserva si apareciera un id sin traducción.
 */

@Composable
fun achievementDescription(achievement: Achievement): String {
    val res = when (achievement.id) {
        "taps_100" -> R.string.ach_taps_100
        "taps_1000" -> R.string.ach_taps_1000
        "taps_10000" -> R.string.ach_taps_10000
        "taps_100000" -> R.string.ach_taps_100000
        "taps_500000" -> R.string.ach_taps_500000
        "taps_1m" -> R.string.ach_taps_1m
        "coins_1000" -> R.string.ach_coins_1000
        "coins_100000" -> R.string.ach_coins_100000
        "coins_1m" -> R.string.ach_coins_1m
        "coins_1b" -> R.string.ach_coins_1b
        "coins_1t" -> R.string.ach_coins_1t
        "coins_1qa" -> R.string.ach_coins_1qa
        "prod_100" -> R.string.ach_prod_100
        "prod_10000" -> R.string.ach_prod_10000
        "prod_1m" -> R.string.ach_prod_1m
        "prod_1b" -> R.string.ach_prod_1b
        "prestige_1" -> R.string.ach_prestige_1
        "prestige_3" -> R.string.ach_prestige_3
        "prestige_5" -> R.string.ach_prestige_5
        "prestige_10" -> R.string.ach_prestige_10
        "combo_25" -> R.string.ach_combo_25
        "worlds_3" -> R.string.ach_worlds_3
        "worlds_6" -> R.string.ach_worlds_6
        "worlds_10" -> R.string.ach_worlds_10
        "workers_1" -> R.string.ach_workers_1
        "workers_5" -> R.string.ach_workers_5
        "workers_15" -> R.string.ach_workers_15
        "workers_30" -> R.string.ach_workers_30
        "workers_50" -> R.string.ach_workers_50
        "workers_100" -> R.string.ach_workers_100
        "strike_1" -> R.string.ach_strike_1
        "strike_5" -> R.string.ach_strike_5
        "strike_20" -> R.string.ach_strike_20
        "strike_50" -> R.string.ach_strike_50
        "contracts_1" -> R.string.ach_contracts_1
        "contracts_5" -> R.string.ach_contracts_5
        "contracts_15" -> R.string.ach_contracts_15
        "pets_1" -> R.string.ach_pets_1
        "pets_3" -> R.string.ach_pets_3
        "pets_5" -> R.string.ach_pets_5
        "pets_10" -> R.string.ach_pets_10
        "pets_max" -> R.string.ach_pets_max
        "breed_1" -> R.string.ach_breed_1
        "breed_5" -> R.string.ach_breed_5
        "breed_15" -> R.string.ach_breed_15
        "breed_30" -> R.string.ach_breed_30
        "breed_50" -> R.string.ach_breed_50
        "mutant_1" -> R.string.ach_mutant_1
        "mutant_3" -> R.string.ach_mutant_3
        "mutant_5" -> R.string.ach_mutant_5
        "mutant_10" -> R.string.ach_mutant_10
        "gen_2" -> R.string.ach_gen_2
        "gen_3" -> R.string.ach_gen_3
        "gen_5" -> R.string.ach_gen_5
        "gen_10" -> R.string.ach_gen_10
        "welfare_90" -> R.string.ach_welfare_90
        "welfare_100" -> R.string.ach_welfare_100
        "traits_5" -> R.string.ach_traits_5
        "traits_10" -> R.string.ach_traits_10
        "traits_all" -> R.string.ach_traits_all
        else -> null
    }
    return if (res != null) stringResource(res) else achievement.description
}

@Composable
fun petMoodName(mood: Pet.PetMood): String = stringResource(
    when (mood) {
        Pet.PetMood.ECSTATIC -> R.string.mood_ecstatic
        Pet.PetMood.HAPPY -> R.string.mood_happy
        Pet.PetMood.NORMAL -> R.string.mood_normal
        Pet.PetMood.SAD -> R.string.mood_sad
        Pet.PetMood.DEPRESSED -> R.string.mood_depressed
        Pet.PetMood.ANGRY -> R.string.mood_angry
        Pet.PetMood.SICK -> R.string.mood_sick
        Pet.PetMood.DYING -> R.string.mood_dying
    }
)
