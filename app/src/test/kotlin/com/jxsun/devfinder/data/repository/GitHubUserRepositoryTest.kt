package com.jxsun.devfinder.data.repository

import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.data.source.DataSource
import com.jxsun.devfinder.data.source.local.LocalDataSource
import com.jxsun.devfinder.data.source.remote.RemoteDataSource
import com.jxsun.devfinder.model.GitHubUser
import com.jxsun.devfinder.model.GitHubUserDetail
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Completable
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

    @Mock
    private lateinit var localDataSource: LocalDataSource

    private lateinit var sut: GitHubUserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = GitHubUserRepository(
            remoteSource = remoteDataSource,
            localSource = localDataSource
        )
    }

    @Test
    fun `fetch remote users and update to the initial local users successfully`() {
        val remoteUser = GitHubUser(
            id = 100,
            loginName = "Josh",
            avatarUrl = "",
            siteAdmin = true
        )
        doReturn(
            Single.just(
                DataSource.UserListData(
                    nextSinceIdx = 11,
                    users = listOf(remoteUser)
                )
            )
        ).`when`(remoteDataSource).getUsers(since = 10)

        // Don't have local copy.
        doReturn(0)
            .`when`(localDataSource).nextUserIndex()

        doReturn(Completable.complete())
            .`when`(localDataSource).saveFetchedUsers(any(), any())

        val testObservable = sut.fetchUsers(since = 10).test()

        testObservable.assertValue { it.nextSinceIdx == 11 }
        testObservable.assertValue { it.users.single() == remoteUser }
        verify(localDataSource).saveFetchedUsers(11, listOf(remoteUser))
    }

    @Test
    fun `fetch the latter remote users successfully`() {
        val user = GitHubUser(
            id = 100,
            loginName = "Josh",
            avatarUrl = "",
            siteAdmin = true
        )
        doReturn(
            Single.just(
                DataSource.UserListData(
                    nextSinceIdx = 11,
                    users = listOf(user)
                )
            )
        ).`when`(remoteDataSource).getUsers(since = 10)

        // Already have initial local copy.
        doReturn(10)
            .`when`(localDataSource).nextUserIndex()

        doReturn(Completable.complete())
            .`when`(localDataSource).saveFetchedUsers(any(), any())

        val testObservable = sut.fetchUsers(since = 10).test()

        testObservable.assertValue { it.nextSinceIdx == 11 }
        testObservable.assertValue { it.users.single() == user }
        verify(localDataSource, never()).saveFetchedUsers(any(), any())
    }

    @Test
    fun `fetch local users successfully`() {
        val localUser = GitHubUser(
            id = 100,
            loginName = "Josh",
            avatarUrl = "",
            siteAdmin = true
        )
        val remoteUser = GitHubUser(
            id = 101,
            loginName = "Josh2",
            avatarUrl = "",
            siteAdmin = true
        )
        doReturn(
            Single.just(
                DataSource.UserListData(
                    nextSinceIdx = 12,
                    users = listOf(remoteUser)
                )
            )
        ).`when`(remoteDataSource).getUsers(since = 0)
        doReturn(200)
            .`when`(localDataSource).nextUserIndex()
        doReturn(
            Single.just(
                DataSource.UserListData(
                    nextSinceIdx = 11,
                    users = listOf(localUser)
                )
            )
        ).`when`(localDataSource).loadCachedUsers()
        doReturn(Completable.complete())
            .`when`(localDataSource).saveFetchedUsers(any(), any())

        val testObservable = sut.fetchUsers(since = 10).test()

        testObservable.assertValueCount(2)
        testObservable.assertValueAt(0) { it.nextSinceIdx == 11 }
        testObservable.assertValueAt(0) { it.users.single() == localUser }
        testObservable.assertValueAt(1) { it.nextSinceIdx == 12 }
        testObservable.assertValueAt(1) { it.users.single() == remoteUser }
        verify(localDataSource).saveFetchedUsers(12, listOf(remoteUser))
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
                DataSource.UserDetailData(userDetail = userDetail)
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
