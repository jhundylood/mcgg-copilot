package com.mcggcopilot.model

import kotlinx.serialization.Serializable

/**
 * One hero's static reference data for a given season.
 *
 * Every field that could not be independently confirmed from the in-game
 * Synergy Guide / Encyclopedia carries a matching `*Verified` flag set to
 * `false`. The scorer and UI MUST check these flags before using the field
 * for a hard decision (see [com.mcggcopilot.scoring.TacticalScorer]).
 */
@Serializable
data class Hero(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val aliasesVerified: Boolean = false,
    val cost: Int?,                    // null = no fixed cost (fully dynamic unlock)
    val costVerified: Boolean = true,
    val faction: String,               // Synergy.id of type FACTION
    val factionVerified: Boolean = true,
    val roles: List<String>,           // Synergy.id(s) of type ROLE - supports dual/multi-role heroes
    val rolesVerified: Boolean = true,
    val purchasable: Boolean = true,   // false for Alpha + the 3 Chrono Wanderer summon-only heroes
    val specialRule: String? = null,
    val specialRuleVerified: Boolean = true,
    val dynamicTags: List<DynamicTag> = emptyList()
) {
    /** True if ANY field on this hero is still unverified against the live game. */
    val hasUnverifiedData: Boolean
        get() = !costVerified || !factionVerified || !rolesVerified || !specialRuleVerified
}

enum class DynamicTag {
    SPECTRE_ALPHA_UNLOCK_CHAIN,
    FROSTRIDER_RIDER,
    ECHOMANCER_SUMMON_POOL,
    DRAGONSLAYER_FIXED,
    DRAGONSLAYER_RANDOM_POOL_CANDIDATE // see mechanicNotes on the Dragonslayer Synergy - pool is [UNVERIFIED]
}

enum class SynergyType { FACTION, ROLE }

@Serializable
data class SynergyTier(
    val count: Int,
    val effectText: String,
    val verified: Boolean = true
)

@Serializable
data class Synergy(
    val id: String,
    val name: String,
    val type: SynergyType,
    val tiers: List<SynergyTier>,
    val mechanicNotes: String? = null,
    val verified: Boolean = true
) {
    /** Highest tier count whose threshold is <= activeCount, or null if below the first tier. */
    fun activeTier(activeCount: Int): SynergyTier? =
        tiers.filter { it.count <= activeCount }.maxByOrNull { it.count }

    fun nextTier(activeCount: Int): SynergyTier? =
        tiers.filter { it.count > activeCount }.minByOrNull { it.count }

    /** A synergy is "minor" if its highest tier requirement is <= 4 (used by Choice Magic Crystal logic). */
    val isMinor: Boolean get() = (tiers.maxOfOrNull { it.count } ?: 0) <= 4
}

@Serializable
data class WheelOfFateModifier(
    val id: String,
    val name: String,
    val description: String,
    val verified: Boolean = true
)

// ---------------------------------------------------------------------------
// Live match state. Built ONLY from on-device OCR of the player's own screen.
// Never persisted beyond the current match except into local win-rate history,
// and never transmitted anywhere.
// ---------------------------------------------------------------------------

data class BoardSlot(
    val heroId: String?,
    val star: Int = 1,
    val items: List<String> = emptyList(),
    val position: Int = -1 // board tile index; -1 for bench slots
)

data class MatchState(
    val stage: String,
    val round: String,
    val level: Int,
    val gold: Int,
    val streak: Int = 0,
    val board: List<BoardSlot> = emptyList(),
    val bench: List<BoardSlot> = emptyList(),
    val shop: List<String?> = emptyList(),      // hero ids currently offered, null = empty slot
    val activeSynergyCounts: Map<String, Int> = emptyMap(), // synergy.id -> number of active heroes
    val wheelOfFateModifierId: String? = null,
    val lastUpdatedAtMs: Long = System.currentTimeMillis()
)

enum class TacticalState { CREEP, SWAP, PIVOT, HOLD }

data class TacticalRecommendation(
    val state: TacticalState,
    val headline: String,
    val rationale: List<String>,
    val confidence: Float,          // 0f..1f
    val usesUnverifiedData: Boolean // true => UI must show the amber "verify in-game" affordance
)
