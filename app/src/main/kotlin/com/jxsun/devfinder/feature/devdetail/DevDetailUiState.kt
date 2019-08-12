package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.base.core.UiState
import com.jxsun.devfinder.model.GitHubUserDetail

/**
 * The concrete UI state for the detailed info page.
 */
data class DevDetailUiState(
        val isLoading: Boolean,
        val userDetail: GitHubUserDetail?,
        val error: Throwable? = null
) : UiState {

    companion object {
        val IDLE = DevDetailUiState(
                isLoading = false,
                userDetail = null,
                error = null
        )
    }
}