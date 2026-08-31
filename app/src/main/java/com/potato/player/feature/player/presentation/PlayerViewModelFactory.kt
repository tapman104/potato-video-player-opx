package com.potato.player.feature.player.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.potato.player.data.VideoHistoryRepository
import com.potato.player.engine.MpvWrapper

// ---------------------------------------------------------------------------
// PlayerViewModelFactory — matches the exact call site in AppNavigation:
//   PlayerViewModelFactory(context.applicationContext, wrapper, historyRepository)
// ---------------------------------------------------------------------------
class PlayerViewModelFactory(
    private val context: Context,
    private val wrapper: MpvWrapper,
    @Suppress("UNUSED_PARAMETER") historyRepository: VideoHistoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == PlayerViewModel::class.java) {
            "PlayerViewModelFactory only creates PlayerViewModel"
        }
        return PlayerViewModel(
            context    = context,
            controller = wrapper,
            engineState = wrapper.engineState
        ) as T
    }
}
