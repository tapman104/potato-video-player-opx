package com.potato.player.feature.player.presentation

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.potato.player.domain.PlayerController

// ---------------------------------------------------------------------------
// VideoSurface — thin wrapper around SurfaceView.
//
// Responsibilities:
//   • Create a SurfaceView and wire its SurfaceHolder callbacks to
//     PlayerController.attachSurface / detachSurface.
//   • Release the surface on Compose dispose.
//
// Non-responsibilities:
//   • No playback logic.
//   • No engine imports.
// ---------------------------------------------------------------------------
@Composable
fun VideoSurface(
    controller: PlayerController,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            SurfaceView(context).also { surfaceView ->
                surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        controller.attachSurface(holder.surface)
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        // MPV handles size changes internally; no action needed.
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        controller.detachSurface()
                    }
                })
            }
        },
        modifier = modifier
    )
}
