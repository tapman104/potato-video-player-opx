package com.potato.player.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// SeekController — owns all seek policy on top of PlayerController.
//
// PlayerController has two raw seek calls:
//   seekFast(ms)     — keyframe-aligned, use during scrub drag
//   seekAccurate(ms) — frame-accurate, use on finger-up / commit
//
// SeekController adds:
//   • Scrub throttle  — seekFast fires at most every THROTTLE_MS during drag
//   • Seek-by         — jump forward/back by fixed step (double-tap gesture)
//   • Drag preview    — accumulates delta while dragging, commits on release
//
// ViewModel calls SeekController, never calls PlayerController seek directly.
// ---------------------------------------------------------------------------
class SeekController(
    private val controller: PlayerController,
    private val scope: CoroutineScope,
    private val getDurationMs: () -> Long
) {

    // ── Constants ─────────────────────────────────────────────────────────────

    companion object {
        /** Minimum ms between seekFast calls during scrub drag. */
        private const val THROTTLE_MS = 200L

        /** Default step for forward/back double-tap or button seek. */
        const val SEEK_STEP_MS = 10_000L
    }

    // ── Scrub state ───────────────────────────────────────────────────────────

    /** True while user finger is on the seek bar. */
    var isScrubbing: Boolean = false
        private set

    /** Last position (ms) committed via seekAccurate. */
    private var lastCommittedMs: Long = 0L

    /** Throttle job — cancelled and relaunched on each drag event. */
    private var throttleJob: Job? = null

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Call when seek bar drag starts.
     * Freezes the progress collector so UI doesn't jump while scrubbing.
     */
    fun onDragStart(currentPositionMs: Long) {
        isScrubbing = true
        lastCommittedMs = currentPositionMs
        // TODO: freeze progress UI here if needed
    }

    /**
     * Call on every seek bar value change during drag.
     * Throttles seekFast to at most once per THROTTLE_MS.
     */
    fun onDragMove(positionMs: Long) {
        if (!isScrubbing) return
        throttleJob?.cancel()
        throttleJob = scope.launch {
            delay(THROTTLE_MS)
            controller.seekFast(positionMs)
        }
    }

    /**
     * Call when seek bar finger is lifted.
     * Cancels any pending throttle and commits with frame-accurate seek.
     */
    fun onDragRelease(positionMs: Long) {
        throttleJob?.cancel()
        isScrubbing = false
        lastCommittedMs = positionMs
        controller.seekAccurate(positionMs)
    }

    /**
     * Jump forward by [stepMs] ms from current position.
     * Clamps to duration. Used by double-tap right / forward button.
     */
    fun seekForward(currentPositionMs: Long, stepMs: Long = SEEK_STEP_MS) {
        val target = (currentPositionMs + stepMs).coerceAtMost(getDurationMs())
        controller.seekAccurate(target)
    }

    /**
     * Jump backward by [stepMs] ms from current position.
     * Clamps to 0. Used by double-tap left / back button.
     */
    fun seekBackward(currentPositionMs: Long, stepMs: Long = SEEK_STEP_MS) {
        val target = (currentPositionMs - stepMs).coerceAtLeast(0L)
        controller.seekAccurate(target)
    }

    /** Cancel any in-flight throttle job. Call from ViewModel.onCleared(). */
    fun cancel() {
        throttleJob?.cancel()
    }
}
