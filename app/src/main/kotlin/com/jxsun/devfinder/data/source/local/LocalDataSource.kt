package com.jxsun.devfinder.data.source.local

import com.jxsun.devfinder.data.source.DataSource
import com.jxsun.devfinder.data.source.local.database.AppDatabase
import com.jxsun.devfinder.model.GitHubUser
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

/**
 * The data provider which accesses the local persistence to offer data.
 */
class LocalDataSource(
    private val database: AppDatabase,
    private val prefs: AppPreferences,
    private val localUserDataMapper: LocalUserDataMapper
) {
    /**
     * Loads the cached user list.
     */
    fun loadCachedUsers(): Single<DataSource.UserListData> {
        return Single.fromCallable {
            prefs.nextUserIndex
        }.flatMap { nextIdx ->
            if (nextIdx == 0) {
                Single.just(
                    DataSource.UserListData(
                        nextSinceIdx = 0,
                        users = emptyList()
                    )
                )
            } else {
                database.userDao().getAll()
                    .map { entityList ->
                        DataSource.UserListData(
                            nextSinceIdx = nextIdx,
                            users = entityList.map(localUserDataMapper::toModel)
                        )
                    }
            }
        }
    }

    /**
     * Saves the [nextUserIndex] and [users] to the local persistence.
     */
    fun saveFetchedUsers(nextUserIndex: Int, users: List<GitHubUser>): Completable {
        return Single.zip(
            Single.fromCallable {
                prefs.nextUserIndex = nextUserIndex
            },
            Single.fromCallable {
                database.userDao().reset(users.map(localUserDataMapper::fromModel))
            },
            BiFunction<Unit, Unit, Boolean> { t1, t2 -> true }
        )
            .subscribeOn(Schedulers.io())
            .ignoreElement()
    }

    /**
     * Gets the cached next user index.
     */
    fun nextUserIndex() = prefs.nextUserIndex
}
