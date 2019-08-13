package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail
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
) {

    private val userListResponseDataParser = ResponseDataParser<List<UserResponse>>()
    private val userDetailResponseDataParser = ResponseDataParser<UserDetailResponse>()

    fun getUsers(
        since: Int
    ): Single<UserListData> {
        return if (networkChecker.isNetworkConnected()) {
            return gitHubService.getAllUsers(since, PER_PAGE)
                .compose(userListResponseDataParser.parse())
                .map {
                    UserListData(
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
    ): Single<UserDetailData> {
        return if (networkChecker.isNetworkConnected()) {
            return gitHubService.getUser(login)
                .compose(userDetailResponseDataParser.parse())
                .map {
                    UserDetailData(
                        userDetail = it.data?.let { response ->
                            remoteUserDetailDataMapper.toModel(response)
                        } ?: throw Throwable("No available userDetail data!")
                    )
                }
        } else {
            Single.error(NoConnectionException())
        }
    }

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
