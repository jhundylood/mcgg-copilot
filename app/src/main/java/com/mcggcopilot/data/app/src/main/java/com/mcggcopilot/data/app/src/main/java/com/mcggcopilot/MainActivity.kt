package com.mcggcopilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mcggcopilot.data.Season8Catalog
import com.mcggcopilot.ocr.RawMatchOcr
import com.mcggcopilot.scoring.CopilotEngine
import com.mcggcopilot.ui.HudStateHolder
import com.mcggcopilot.ui.hub.HubUiState
import com.mcggcopilot.ui.hub.MainHubScreen
import com.mcggcopilot.ui.hub.OverlayServiceStatus

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var serviceStatus by remember { mutableStateOf(OverlayServiceStatus.STOPPED) }

            val catalog = remember {
                Season8Catalog(
                    heroes = emptyList(),
                    synergies = emptyList(),
                    modifiers = emptyList()
                )
            }
            val engine = remember(catalog) { CopilotEngine(catalog) }
            val hudStateHolder = remember(engine) { HudStateHolder(engine) }
            var hudUiState by remember { mutableStateOf(hudStateHolder.uiState) }

            val unverifiedHeroCount = remember(catalog) {
                catalog.heroes.count { it.hasUnverifiedData }
            }

            val activeBoardSummary = if (serviceStatus == OverlayServiceStatus.RUNNING) {
                val verifyBadge = if (hudUiState.usesUnverifiedData) " · Verify in-game" else ""
                "${hudUiState.tacticalState.name}: ${hudUiState.headline} (${hudUiState.roundLabel} · ${hudUiState.gold}g$verifyBadge)"
            } else {
                null
            }

            val uiState = HubUiState(
                serviceStatus = serviceStatus,
                datasetSeasonLabel = "Season 8 · Grand Magic Clash",
                datasetHeroCount = catalog.heroes.size.coerceAtLeast(56),
                datasetUnverifiedFieldCount = unverifiedHeroCount,
                activeBoardSummary = activeBoardSummary,
                topLineups = emptyList()
            )

            MainHubScreen(
                state = uiState,
                onToggleService = {
                    serviceStatus = if (serviceStatus == OverlayServiceStatus.RUNNING) {
                        OverlayServiceStatus.STOPPED
                    } else {
                        hudUiState = hudStateHolder.updateFromOcr(
                            RawMatchOcr(
                                rawStage = "1",
                                rawRound = "1-1",
                                rawLevel = "4",
                                rawGold = "10"
                            )
                        )
                        OverlayServiceStatus.RUNNING
                    }
                },
                onOpenRosterEditor = { /* TODO: open roster editor */ },
                onOpenCardBiasConfig = { /* TODO: open Go Go card bias config */ },
                onOpenAnalytics = { /* TODO: open match history analytics */ }
            )
        }
    }
}
