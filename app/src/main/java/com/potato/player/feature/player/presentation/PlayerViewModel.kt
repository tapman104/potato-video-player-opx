package com.potato.player.feature.player.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.potato.player.domain.PlayerController
import com.potato.player.engine.PlayerEngineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.potato.player.domain.SeekController

// ---------------------------------------------------------------------------
// VideoFitMode — declared here so PlayerBottomControls can import from the
// feature.player package as expected by its existing import statement.
// ---------------------------------------------------------------------------
enum class VideoFitMode { FIT, FILL, STRETCH }

// ---------------------------------------------------------------------------
// PlaybackProgressState — what PlayerBottomControls collects via progressState.
//
// Field names must match exactly what PlayerBottomControls reads:
//   positionSec, durationSec, cachedSec, cacheDurationSec
// ---------------------------------------------------------------------------
data class PlaybackProgressState(
    val positionSec: Double = 0.0,
    val durationSec: Double = 0.0,
    val cachedSec: Double = 0.0,
    val cacheDurationSec: Double = 0.0
)

// ---------------------------------------------------------------------------
// PlayerViewModel
//
// Owns:
//   • progressState  — position / duration / cache for PlayerBottomControls
//   • isPlaying      — pause state for PlayerCenterPlayPause
//   • Seek interaction: onSliderDragStart, onSeekGesture, onSeekCommit
//   • loadFile       — called once from PlayerScreen on first composition
// ---------------------------------------------------------------------------
class PlayerViewModel(
    @Suppress("UNUSED_PARAMETER") context: Context,
    val controller: PlayerController,
    engineState: StateFlow<PlayerEngineState>
) : ViewModel() {

    private val seekController = SeekController(
        controller = controller,
        scope = viewModelScope,
        getDurationMs = { (_progressState.value.durationSec * 1000).toLong() }
    )

    // ── Playback progress ─────────────────────────────────────────────────────

    private val _progressState = MutableStateFlow(PlaybackProgressState())
    val progressState: StateFlow<PlaybackProgressState> = _progressState.asStateFlow()

    // ── Pause / playing ───────────────────────────────────────────────────────

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // ── Controls visibility (auto-hide) ───────────────────────────────────────
    private val _controlsVisible = MutableStateFlow(true)
    val controlsVisible: StateFlow<Boolean> = _controlsVisible.asStateFlow()

    private var hideControlsJob: Job? = null

    // ── Seek-bar drag guard ───────────────────────────────────────────────────
    //
    // While the user is dragging, engineState position updates are suppressed
    // so the seek bar doesn't fight the user's finger.

    // ── Engine state collector ────────────────────────────────────────────────

    init {
        engineState
            .onEach { state ->
                _isPlaying.update { !state.paused }
                if (!seekController.isScrubbing) {
                    _progressState.update {
                        PlaybackProgressState(
                            positionSec     = state.positionMs / 1000.0,
                            durationSec     = state.durationMs / 1000.0,
                            cachedSec       = state.cacheTimeMs / 1000.0,
                            cacheDurationSec = state.cacheDurMs / 1000.0
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Seek-bar interaction ──────────────────────────────────────────────────

    fun onSliderDragStart(currentPositionMs: Long) {
        seekController.onDragStart(currentPositionMs)
    }

    fun onSeekGesture(positionMs: Long) {
        seekController.onDragMove(positionMs)
    }

    fun onSeekCommit(positionMs: Long) {
        seekController.onDragRelease(positionMs)
    }

    fun seekForward(currentPositionMs: Long) {
        seekController.seekForward(currentPositionMs)
    }

    fun seekBackward(currentPositionMs: Long) {
        seekController.seekBackward(currentPositionMs)
    }

    /**
     * Call this on every user interaction (tap, seek drag, any gesture).
     * Shows controls immediately and restarts the 5-second hide timer.
     * If locked is true, controls stay hidden — caller must check lock state.
     */
    fun onUserInteraction() {
        _controlsVisible.value = true
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(5_000L)
            _controlsVisible.value = false
        }
    }

    fun hideControls() {
        hideControlsJob?.cancel()
        _controlsVisible.value = false
    }

    /** Call this when the player is paused — controls stay visible, timer stops. */
    fun onPlaybackPaused() {
        _controlsVisible.value = true
        hideControlsJob?.cancel()
    }

    /** Call this when the player resumes — restart the hide timer. */
    fun onPlaybackResumed() {
        onUserInteraction()
    }

    // ── Playback controls ─────────────────────────────────────────────────────

    fun togglePlay() {
        controller.togglePlay()
    }

    fun loadFile(uri: String) {
        controller.loadFile(uri)
    }

    override fun onCleared() {
        super.onCleared()
        seekController.cancel()
    }

    // ── Phase 2 stubs ─────────────────────────────────────────────────────────

    // TODO: Phase 2 — audio/subtitle track selection
    // TODO: Phase 2 — playback speed
    // TODO: Phase 2 — PiP entry
    // TODO: Phase 2 — screen lock
    // TODO: Phase 2 — playlist prev/next
    // TODO: Phase 2 — resume position from history
}
