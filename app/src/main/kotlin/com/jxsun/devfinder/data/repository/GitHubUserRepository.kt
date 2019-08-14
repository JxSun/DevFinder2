package com.jxsun.devfinder.data.repository

import com.jxsun.devfinder.data.source.local.LocalDataSource
import com.jxsun.devfinder.data.source.remote.RemoteDataSource
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * The repository which provides [GitHubUser] data.
 */
class GitHubUserRepository(
    private val remoteSource: RemoteDataSource,
    private val localSource: LocalDataSource
) {

    /**
     * Fetches the [GitHubUser]s since the given index and wraps the result into [FetchUsersResult].
     *
     * To speed up the process of fetching the initial data. I save the the remote users in the
     * first page to local. Every time the app relaunches, the local copy will be provided first,
     * and then I will also try to fetch the remote to get the latest data and provide to the upper
     * layer.
     */
    fun fetchUsers(since: Int): Observable<FetchUsersResult> {
        return Observable.fromCallable {
            since >= localSource.nextUserIndex() || localSource.nextUserIndex() == 0
        }.flatMap { loadByRemote ->
            Timber.d("fetch from remote: $loadByRemote")
            if (loadByRemote) {
                remoteSource.getUsers(since = since)
                    .toObservable()
                    .flatMap {
                        // Copy back to local if it's the initial page data.
                        if (localSource.nextUserIndex() == 0) {
                            localSource.saveFetchedUsers(
                                nextUserIndex = it.nextSinceIdx,
                                users = it.users
                            ).toSingle { it }.toObservable()
                        } else {
                            Observable.just(it)
                        }
                    }
            } else {
                Observable.merge(
                    // Trigger local fetching first.
                    localSource.loadCachedUsers().toObservable(),
                    // Then try to fetch from remote to get the latest data.
                    remoteSource.getUsers(0).toObservable()
                        .flatMap {
                            // Copy back to local.
                            localSource.saveFetchedUsers(
                                nextUserIndex = it.nextSinceIdx,
                                users = it.users
                            ).toSingle { it }.toObservable()
                        }
                )
            }
        }
            .subscribeOn(Schedulers.io())
            .map { sourceData ->
                FetchUsersResult(
                    users = sourceData.users,
                    nextSinceIdx = sourceData.nextSinceIdx
                )
            }
            .doOnError {
                Timber.w(it, "failed to fetch users")
            }
    }

    fun fetchUserDetail(login: String): Single<FetchUserDetailResult> {
        return remoteSource.getUserDetail(login = login)
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
