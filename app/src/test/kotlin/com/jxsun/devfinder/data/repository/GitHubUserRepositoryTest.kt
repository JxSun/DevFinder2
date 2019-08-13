package com.jxsun.devfinder.data.repository

import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.data.source.RemoteDataSource
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail
import com.nhaarman.mockitokotlin2.doReturn
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class GitHubUserRepositoryTest {

    @Rule
    @JvmField
    val schedulers = RxImmediateSchedulerRule()

    @Mock
    private lateinit var remoteDataSource: RemoteDataSource

    private lateinit var sut: GitHubUserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = GitHubUserRepository(
            dataSource = remoteDataSource
        )
    }

    @Test
    fun `fetch users successfully`() {
        val user = GitHubUser(
            id = 100,
            loginName = "Josh",
            avatarUrl = "",
            siteAdmin = true
        )
        doReturn(
            Single.just(
                RemoteDataSource.UserListData(
                    nextSinceIdx = 11,
                    users = listOf(user)
                )
            )
        ).`when`(remoteDataSource).getUsers(since = 10)

        val testObservable = sut.fetchUsers(since = 10).test()

        testObservable.assertValue { it.nextSinceIdx == 11 }
        testObservable.assertValue { it.users.single() == user }
    }

    @Test
    fun `fetch users failed`() {
        val exception = Exception("test")
        doReturn(Single.fromCallable { throw exception })
            .`when`(remoteDataSource)
            .getUsers(since = 10)

        val testObservable = sut.fetchUsers(since = 10).test()

        testObservable.assertError { it == exception }
    }

    @Test
    fun `fetch user detailed info successfully`() {
        val userDetail = GitHubUserDetail(
            id = 100,
            loginName = "Josh",
            avatarUrl = "",
            siteAdmin = true,
            name = "Joshua Sun",
            blog = "jxsun.github.io",
            location = "Washington DC",
            bio = "An Android developer"
        )
        doReturn(
            Single.just(
                RemoteDataSource.UserDetailData(userDetail = userDetail)
            )
        ).`when`(remoteDataSource).getUserDetail(login = "Josh")

        val testObservable = sut.fetchUserDetail(login = "Josh").test()

        testObservable.assertValue { it.user == userDetail }
    }

    @Test
    fun `fetch user detailed info failed`() {
        val exception = Exception("test")
        doReturn(Single.fromCallable { throw exception })
            .`when`(remoteDataSource).getUserDetail(login = "Josh")

        val testObservable = sut.fetchUserDetail(login = "Josh").test()

        testObservable.assertError { it == exception }
    }
}
