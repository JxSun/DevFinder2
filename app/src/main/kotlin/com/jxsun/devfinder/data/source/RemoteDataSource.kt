package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.util.NetworkChecker
import io.reactivex.Single

/**
 * The data provider which accesses the server to offer data.
 */
class RemoteDataSource(
        private val gitHubService: GitHubService,
        private val remoteDataMapper: RemoteDataMapper,
        private val networkChecker: NetworkChecker
) {

    private val responseDataParser = ResponseDataParser<List<UserResponse>>()

    fun getUsers(
            since: Int
    ): Single<UserData> {
        return if (networkChecker.isNetworkConnected()) {
            return gitHubService.getAllUsers(since)
                    .compose(responseDataParser.parse())
                    .map {
                        UserData(
                                nextSinceIdx = it.nextSinceIdx,
                                users = it.data?.map(remoteDataMapper::toModel) ?: emptyList()
                        )
                    }
        } else {
            Single.error(NoConnectionException())
        }
    }

    /**
     * The representation of the provided data.
     */
    data class UserData(
            val nextSinceIdx: Int,
            val users: List<GitHubUser>
    )
}