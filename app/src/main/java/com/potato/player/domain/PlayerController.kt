package com.potato.player.domain

import android.view.Surface

// ---------------------------------------------------------------------------
// PlayerController — surface + playback commands abstraction.
//
// MpvWrapper implements this interface so the presentation layer never needs
// to import anything from the engine package directly.
// ---------------------------------------------------------------------------
interface PlayerController {

    // ── Surface ───────────────────────────────────────────────────────────────

    fun attachSurface(surface: Surface)
    fun detachSurface()

    // ── Playback ──────────────────────────────────────────────────────────────

    fun loadFile(uri: String)
    fun play()
    fun pause()
    fun togglePlay()

    // ── Seeking ───────────────────────────────────────────────────────────────

    /** Keyframe-aligned. Use during continuous scrubbing. */
    fun seekFast(ms: Long)

    /** Frame-accurate. Use on seek-bar finger-up / resume position. */
    fun seekAccurate(ms: Long)
}
