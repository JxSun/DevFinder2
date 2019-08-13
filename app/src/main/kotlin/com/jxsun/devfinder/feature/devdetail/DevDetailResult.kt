package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.base.core.Result
import com.jxsun.devfinder.model.GitHubUserDetail

/**
 * The concrete business logic result for the detailed info page.
 */
sealed class DevDetailResult : Result {

    /**
     * Represents the result of getting user's detailed info.
     */
    data class GetUserDetailResult(
        val isLoading: Boolean,
        val userDetail: GitHubUserDetail?,
        val error: Throwable? = null
    ) : DevDetailResult() {

        companion object {
            fun success(
                userDetail: GitHubUserDetail
            ) = GetUserDetailResult(
                isLoading = false,
                userDetail = userDetail,
                error = null
            )

            fun failure(
                error: Throwable
            ) = GetUserDetailResult(
                isLoading = false,
                userDetail = null,
                error = error
            )

            fun inFlight() = GetUserDetailResult(
                isLoading = true,
                userDetail = null,
                error = null
            )
        }
    }
}
