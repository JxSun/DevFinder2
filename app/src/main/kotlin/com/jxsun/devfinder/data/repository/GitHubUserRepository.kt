package com.jxsun.devfinder.data.repository

import com.jxsun.devfinder.data.source.RemoteDataSource
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * The repository which provides [GitHubUser] data.
 */
class GitHubUserRepository(
        private val dataSource: RemoteDataSource
) {
    /**
     * Fetches the [GitHubUser]s since the given index and wraps the result into [FetchUsersResult].
     */
    fun fetchUsers(since: Int): Single<FetchUsersResult> {
        return dataSource.getUsers(since = since)
                .subscribeOn(Schedulers.io())
                .map { response ->
                    FetchUsersResult(
                            users = response.users,
                            nextSinceIdx = response.nextSinceIdx
                    )
                }
                .doOnError {
                    Timber.w(it, "failed to fetch users")
                }
    }

    fun fetchUserDetail(login: String): Single<FetchUserDetailResult> {
        return dataSource.getUserDetail(login = login)
                .subscribeOn(Schedulers.io())
                .map { response ->
                    FetchUserDetailResult(
                            user = response.userDetail
                    )
                }
                .doOnError {
                    Timber.w(it, "failed to fetch user detail")
                }
    }

    /**
     * The fetched user list result representation.
     */
    data class FetchUsersResult(
            val users: List<GitHubUser>,
            val nextSinceIdx: Int
    )

    /**
     * The fetched user detailed info result representation.
     */
    data class FetchUserDetailResult(
            val user: GitHubUserDetail
    )
}