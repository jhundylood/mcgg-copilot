package com.mcggcopilot.data

import com.mcggcopilot.model.DynamicTag
import com.mcggcopilot.model.Hero
import com.mcggcopilot.model.Synergy
import com.mcggcopilot.model.SynergyTier
import com.mcggcopilot.model.SynergyType
import com.mcggcopilot.model.WheelOfFateModifier

/**
 * Pure, offline reference index for Season 8 static data.
 * Resolves IDs, hero names, OCR aliases, and synergies deterministically
 * without performing any file or network I/O.
 */
class Season8Catalog(
    val heroes: List<Hero>,
    val synergies: List<Synergy>,
    val modifiers: List<WheelOfFateModifier> = emptyList()
) {
    val heroById: Map<String, Hero> = heroes.associateBy { it.id }
    val synergyById: Map<String, Synergy> = synergies.associateBy { it.id }
    val modifierById: Map<String, WheelOfFateModifier> = modifiers.associateBy { it.id }

    private val heroByNormalizedKey: Map<String, Hero> = buildMap {
        for (hero in heroes) {
            putIfAbsent(normalize(hero.id), hero)
            putIfAbsent(normalize(hero.name), hero)
            for (alias in hero.aliases) {
                val key = normalize(alias)
                if (key.isNotEmpty()) {
                    putIfAbsent(key, hero)
                }
            }
        }
    }

    private val synergyByNormalizedKey: Map<String, Synergy> = buildMap {
        for (synergy in synergies) {
            putIfAbsent(normalize(synergy.id), synergy)
            putIfAbsent(normalize(synergy.name), synergy)
        }
    }

    fun findHero(rawQuery: String?): Hero? {
        if (rawQuery.isNullOrBlank()) return null
        val key = normalize(rawQuery)
        return heroById[rawQuery] ?: heroByNormalizedKey[key]
    }

    fun findSynergy(rawQuery: String?): Synergy? {
        if (rawQuery.isNullOrBlank()) return null
        val key = normalize(rawQuery)
        return synergyById[rawQuery] ?: synergyByNormalizedKey[key]
    }

    fun purchasableHeroes(): List<Hero> = heroes.filter { it.purchasable }

    fun heroesForSynergy(synergyId: String): List<Hero> =
        heroes.filter { it.faction == synergyId || synergyId in it.roles }

    companion object {
        fun normalize(value: String): String =
            value.lowercase()
                .replace(Regex("[^a-z0-9\\s]"), " ")
                .trim()
                .replace(Regex("\\s+"), " ")
    }
}

class SeasonDataRepository(
    private val catalog: Season8Catalog? = null
) {
    fun getCatalog(): Season8Catalog = catalog ?: defaultSeason8Catalog

    companion object {
        val defaultSeason8Catalog: Season8Catalog by lazy {
            val synergies = listOf(
                Synergy(
                    id = "fairy_tail",
                    name = "Fairy Tail",
                    type = SynergyType.FACTION,
                    tiers = listOf(
                        SynergyTier(2, "Fairy Tail synergy tier 1", verified = false),
                        SynergyTier(4, "Fairy Tail synergy tier 2", verified = false),
                        SynergyTier(6, "Fairy Tail synergy tier 3", verified = false)
                    ),
                    verified = false
                ),
                Synergy(
                    id = "titan",
                    name = "Titan",
                    type = SynergyType.FACTION,
                    tiers = listOf(
                        SynergyTier(2, "+5% Titan DMG Increase; Super Mecha +15% DMG Increase & +5% DMG Reduction", verified = false),
                        SynergyTier(4, "+15% Titan DMG Increase; Super Mecha +30% DMG Increase & +10% DMG Reduction", verified = false),
                        SynergyTier(6, "+30% Titan DMG Increase; Super Mecha +45% DMG Increase & +25% DMG Reduction", verified = false),
                        SynergyTier(10, "All Titans become Super Mecha with full equipment", verified = false)
                    ),
                    mechanicNotes = "One Titan transforms into a Super Mecha in battle.",
                    verified = false
                ),
                Synergy(
                    id = "frostrider",
                    name = "Frostrider",
                    type = SynergyType.FACTION,
                    tiers = listOf(
                        SynergyTier(2, "+15% ATK Speed; Rider inherits 35% of Mount Hybrid ATK", verified = false),
                        SynergyTier(4, "+20% ATK Speed; Rider inherits 50% of Mount Hybrid ATK", verified = false),
                        SynergyTier(6, "+30% ATK Speed; Rider inherits 65% of Mount Hybrid ATK", verified = false)
                    ),
                    mechanicNotes = "Pairs Mounts and Riders; Riders mount up at battle start.",
                    verified = false
                ),
                Synergy(
                    id = "dragonslayer",
                    name = "Dragonslayer",
                    type = SynergyType.FACTION,
                    tiers = listOf(
                        SynergyTier(2, "+10% Hybrid ATK; Dragon Soul stacks grant Shield & Hybrid ATK", verified = false),
                        SynergyTier(4, "+15% Hybrid ATK; higher Dragon Soul stack bonus", verified = false),
                        SynergyTier(6, "+30% Hybrid ATK; max Dragon Soul stack bonus", verified = false)
                    ),
                    mechanicNotes = "Fixed core plus random 1-gold and 5-gold pool candidates per match.",
                    verified = false
                ),
                Synergy(
                    id = "dawnbringer",
                    name = "Dawnbringer",
                    type = SynergyType.FACTION,
                    tiers = listOf(
                        SynergyTier(2, "Dawnbringer tier 1", verified = false),
                        SynergyTier(4, "Dawnbringer tier 2", verified = false)
                    ),
                    verified = false
                ),
                Synergy(
                    id = "deadeye",
                    name = "Deadeye",
                    type = SynergyType.ROLE,
                    tiers = listOf(
                        SynergyTier(2, "Allies +5% Crit Chance; Deadeye +15% Crit Chance & +30% Crit DMG", verified = false),
                        SynergyTier(4, "Allies +10% Crit Chance; Deadeye +25% Crit Chance & +60% Crit DMG", verified = false),
                        SynergyTier(6, "Allies +20% Crit Chance; Deadeye +40% Crit Chance & +120% Crit DMG", verified = false)
                    ),
                    verified = false
                )
            )

            val heroes = listOf(
                Hero(
                    id = "natsu",
                    name = "Natsu Dragneel",
                    aliases = listOf("natsu", "natsu dragneel"),
                    cost = null,
                    costVerified = false,
                    faction = "fairy_tail",
                    factionVerified = false,
                    roles = emptyList(),
                    rolesVerified = false
                ),
                Hero(
                    id = "irithel",
                    name = "Irithel",
                    aliases = listOf("irithel"),
                    cost = 2,
                    costVerified = false,
                    faction = "frostrider",
                    factionVerified = false,
                    roles = listOf("deadeye"),
                    rolesVerified = false,
                    dynamicTags = listOf(DynamicTag.FROSTRIDER_RIDER)
                ),
                Hero(
                    id = "bruno",
                    name = "Bruno",
                    aliases = listOf("bruno"),
                    cost = 3,
                    costVerified = false,
                    faction = "titan",
                    factionVerified = false,
                    roles = listOf("deadeye"),
                    rolesVerified = false
                ),
                Hero(
                    id = "kimmy",
                    name = "Kimmy",
                    aliases = listOf("kimmy"),
                    cost = 4,
                    costVerified = false,
                    faction = "dragonslayer",
                    factionVerified = false,
                    roles = listOf("deadeye"),
                    rolesVerified = false,
                    dynamicTags = listOf(DynamicTag.DRAGONSLAYER_FIXED)
                ),
                Hero(
                    id = "terizla",
                    name = "Terizla",
                    aliases = listOf("terizla"),
                    cost = 5,
                    costVerified = false,
                    faction = "dawnbringer",
                    factionVerified = false,
                    roles = emptyList(),
                    rolesVerified = false,
                    dynamicTags = listOf(DynamicTag.DRAGONSLAYER_RANDOM_POOL_CANDIDATE)
                )
            )

            Season8Catalog(
                heroes = heroes,
                synergies = synergies,
                modifiers = emptyList()
            )
        }
    }
}
