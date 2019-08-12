package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.core.Result
import com.jxsun.devfinder.model.GitHubUser

/**
 * The concrete business logic result for the list page.
 */
sealed class DevListResult : Result {

    /**
     * Represents the result of loading users.
     */
    data class LoadUsersResult(
            val isLoading: Boolean,
            val devList: List<GitHubUser>,
            val nextSinceIdx: Int,
            val error: Throwable? = null
    ) : DevListResult() {

        companion object {
            fun success(
                    devList: List<GitHubUser>,
                    nextSinceIdx: Int
            ) = LoadUsersResult(
                    isLoading = false,
                    devList = devList,
                    nextSinceIdx = nextSinceIdx
            )

            fun failure(
                    error: Throwable
            ) = LoadUsersResult(
                    isLoading = false,
                    devList = listOf(),
                    nextSinceIdx = 0,
                    error = error
            )

            fun inFlight() = LoadUsersResult(
                    isLoading = true,
                    devList = listOf(),
                    nextSinceIdx = 0,
                    error = null
            )
        }
    }
}