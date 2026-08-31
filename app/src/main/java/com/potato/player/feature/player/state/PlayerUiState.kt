package com.potato.player.feature.player.state

data class TrackUiModel(
    val id: Int,
    val displayLabel: String
)

data class PlayerUiState(
    val subScale: Double = 1.0,
    val subPos: Int = 100
)
