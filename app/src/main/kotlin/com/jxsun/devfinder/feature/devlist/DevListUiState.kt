package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.core.UiState
import com.jxsun.devfinder.model.GitHubUser

/**
 * The concrete UI state for the list page.
 */
data class DevListUiState(
        val isLoading: Boolean,
        val devList: List<GitHubUser>,
        val nextSinceIdx: Int,
        val error: Throwable? = null
) : UiState {

    companion object {
        val IDLE = DevListUiState(
                isLoading = false,
                devList = emptyList(),
                nextSinceIdx = 0,
                error = null
        )
    }
}