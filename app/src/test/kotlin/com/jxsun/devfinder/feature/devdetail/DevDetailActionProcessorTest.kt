package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.data.repository.GitHubUserRepository
import com.jxsun.devfinder.model.GitHubUserDetail
import com.nhaarman.mockitokotlin2.doReturn
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DevDetailActionProcessorTest {

    @Rule
    @JvmField
    val schedulers = RxImmediateSchedulerRule()

    @Mock
    private lateinit var repository: GitHubUserRepository

    private lateinit var sut: DevDetailActionProcessor

    private val userDetail = GitHubUserDetail(
        id = 100,
        loginName = "Josh",
        name = "Joshua Sun",
        bio = "An Android developer",
        avatarUrl = "",
        siteAdmin = true,
        blog = "jxsun.github.io",
        location = "Mountain View, CA"
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = DevDetailActionProcessor(repository)
    }

    @Test
    fun `perform get detailed info action and fetch data back successfully`() {
        doReturn(Single.just(GitHubUserRepository.FetchUserDetailResult(user = userDetail)))
            .`when`(repository)
            .fetchUserDetail("Josh")

        val testObserver = Observable.just(DevDetailAction.GetUserDetailAction(login = "Josh"))
            .compose(sut.process)
            .test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(
            0,
            DevDetailResult.GetUserDetailResult(
                isLoading = true,
                userDetail = null,
                error = null
            )
        )
        testObserver.assertValueAt(
            1,
            DevDetailResult.GetUserDetailResult(
                isLoading = false,
                userDetail = userDetail,
                error = null
            )
        )
    }

    @Test
    fun `perform get detailed info action and fetch data back failed`() {
        val exception = Exception("test")
        doReturn(Single.fromCallable { throw exception })
            .`when`(repository)
            .fetchUserDetail("Josh")

        val testObserver = Observable.just(DevDetailAction.GetUserDetailAction(login = "Josh"))
            .compose(sut.process)
            .test()

        testObserver.assertValueCount(2)
        testObserver.assertValueAt(
            0,
            DevDetailResult.GetUserDetailResult(
                isLoading = true,
                userDetail = null,
                error = null
            )
        )
        testObserver.assertValueAt(
            1,
            DevDetailResult.GetUserDetailResult(
                isLoading = false,
                userDetail = null,
                error = exception
            )
        )
    }
}
