package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail

/**
 * The data provider.
 */
interface DataSource {
    /**
     * The representation of the provided user list data.
     */
    data class UserListData(
        val nextSinceIdx: Int,
        val users: List<GitHubUser>
    )

    /**
     * The representation of the provided user detailed info data.
     */
    data class UserDetailData(
        val userDetail: GitHubUserDetail
    )
}
