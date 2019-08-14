package com.jxsun.devfinder.data.source.remote

import com.jxsun.devfinder.data.source.DataSource
import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.util.NetworkChecker
import io.reactivex.Single

private const val PER_PAGE = 20

/**
 * The data provider which accesses the server to offer data.
 */
class RemoteDataSource(
    private val gitHubService: GitHubService,
    private val remoteUserDataMapper: RemoteUserDataMapper,
    private val remoteUserDetailDataMapper: RemoteUserDetailDataMapper,
    private val networkChecker: NetworkChecker
) : DataSource {

    private val userListResponseDataParser =
        ResponseDataParser<List<UserResponse>>()
    private val userDetailResponseDataParser =
        ResponseDataParser<UserDetailResponse>()

    fun getUsers(
        since: Int
    ): Single<DataSource.UserListData> {
        return if (networkChecker.isNetworkConnected()) {
            return gitHubService.getAllUsers(since, PER_PAGE)
                .compose(userListResponseDataParser.parse())
                .map {
                    DataSource.UserListData(
                        nextSinceIdx = it.getNextSinceIndex(),
                        users = it.data?.map(remoteUserDataMapper::toModel) ?: emptyList()
                    )
                }
        } else {
            Single.error(NoConnectionException())
        }
    }

    fun getUserDetail(
        login: String
    ): Single<DataSource.UserDetailData> {
        return if (networkChecker.isNetworkConnected()) {
            return gitHubService.getUser(login)
                .compose(userDetailResponseDataParser.parse())
                .map {
                    DataSource.UserDetailData(
                        userDetail = it.data?.let { response ->
                            remoteUserDetailDataMapper.toModel(response)
                        } ?: throw Throwable("No available userDetail data!")
                    )
                }
        } else {
            Single.error(NoConnectionException())
        }
    }
}
