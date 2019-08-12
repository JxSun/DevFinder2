package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.data.repository.GitHubUserRepository
import com.jxsun.devfinder.model.GitHubUser
import com.nhaarman.mockitokotlin2.doReturn
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DevListActionProcessorTest {

    @Rule
    @JvmField
    val schedulers = RxImmediateSchedulerRule()

    @Mock
    private lateinit var repository: GitHubUserRepository

    private lateinit var sut: DevListActionProcessor

    private val users = listOf(GitHubUser(
            id = 9999,
            loginName = "Josh",
            avatarUrl = "https://localhost",
            siteAdmin = false
    ))

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = DevListActionProcessor(repository)
    }

    @Test
    fun `perform load more action and fetch data back successfully`() {
        doReturn(Single.just(GitHubUserRepository.FetchUsersResult(users = users, nextSinceIdx = 50)))
                .`when`(repository)
                .fetchUsers(49)

        val testObserver = Observable.just(DevListAction.LoadUsersAction(sinceIndex = 49))
                .compose(sut.process)
                .test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, DevListResult.LoadUsersResult(isLoading = true, devList = emptyList(), nextSinceIdx = 0, error = null))
        testObserver.assertValueAt(1, DevListResult.LoadUsersResult(isLoading = false, devList = users, nextSinceIdx = 50, error = null))
    }

    @Test
    fun `perform load more action and fetch data back failed`() {
        val exception = Exception("test")
        doReturn(Single.fromCallable { throw exception })
                .`when`(repository)
                .fetchUsers(49)

        val testObserver = Observable.just(DevListAction.LoadUsersAction(sinceIndex = 49))
                .compose(sut.process)
                .test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(0, DevListResult.LoadUsersResult(isLoading = true, devList = emptyList(), nextSinceIdx = 0, error = null))
        testObserver.assertValueAt(1, DevListResult.LoadUsersResult(isLoading = false, devList = emptyList(), nextSinceIdx = 0, error = exception))
    }
}
