package com.potato.player.feature.player.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.potato.player.feature.player.PlayerViewModel
import com.potato.player.feature.player.controls.PlayerBottomControls
import com.potato.player.feature.player.controls.PlayerCenterPlayPause

// ---------------------------------------------------------------------------
// PlayerScreen
//
// Layout (back to front):
//   1. VideoSurface — full-screen SurfaceView
//   2. PlayerCenterPlayPause — centred play/pause button
//   3. PlayerBottomControls — seek bar + transport row
//
// No engine imports. No MpvWrapper imports.
// ---------------------------------------------------------------------------
@Composable
fun PlayerScreen(
    videoUri: String,
    title: String,
    viewModel: PlayerViewModel,
    isExternalIntent: Boolean,
    playlist: List<String>,
    playlistTitles: List<String>,
    onBack: () -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    // Load the file once when the URI first arrives.
    LaunchedEffect(videoUri) {
        viewModel.loadFile(videoUri)
    }

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. Video output surface — fills the entire Box
        VideoSurface(
            controller = viewModel.asController(),
            modifier   = Modifier.fillMaxSize()
        )

        // 2. Centre play/pause button
        PlayerCenterPlayPause(
            isPlaying = isPlaying,
            onClick   = { viewModel.togglePlay() },
            modifier  = Modifier.align(Alignment.Center)
        )

        // 3. Bottom seek + transport controls
        PlayerBottomControls(
            viewModel     = viewModel,
            onSeekGesture = { ms -> viewModel.onSeekGesture(ms) },
            onSeekCommit  = { ms -> viewModel.onSeekCommit(ms) },
            onDragEnd     = { /* isScrubbing is reset inside onSeekCommit */ },
            modifier      = Modifier.align(Alignment.BottomCenter)
        )
    }
}
