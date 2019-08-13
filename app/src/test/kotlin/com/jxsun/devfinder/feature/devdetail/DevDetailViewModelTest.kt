package com.jxsun.devfinder.feature.devdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.model.GitHubUserDetail
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class DevDetailViewModelTest {

    @Rule
    @JvmField
    val schedulers = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var actionProcessor: DevDetailActionProcessor

    @Mock
    lateinit var stateObserver: Observer<DevDetailUiState>

    private lateinit var sut: DevDetailViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = DevDetailViewModel(actionProcessor)
    }

    @Test
    fun `process get detailed info event`() {
        val userDetail = GitHubUserDetail(
            id = 100,
            loginName = "Josh",
            name = "Joshua Sun",
            bio = "An Android developer",
            avatarUrl = "",
            siteAdmin = true,
            blog = "jxsun.github.io",
            location = "Mountain View, CA"
        )
        val processedResult = ObservableTransformer<DevDetailAction, DevDetailResult> {
            it.flatMap {
                Observable.fromArray(
                    DevDetailResult.GetUserDetailResult(
                        isLoading = true, userDetail = null, error = null
                    ),
                    DevDetailResult.GetUserDetailResult(
                        isLoading = false, userDetail = userDetail, error = null
                    )
                )
            }
        }
        doReturn(processedResult).`when`(actionProcessor).process

        sut.state.observeForever(stateObserver)

        sut.fireEvent(DevDetailUiEvent.GetUserDetailEvent(login = "Josh"))

        verify(actionProcessor).process

        argumentCaptor<DevDetailUiState>().run {
            verify(stateObserver, times(3)).onChanged(capture())

            assertEquals(DevDetailUiState.IDLE, allValues[0])
            assertEquals(DevDetailUiState.IDLE.copy(isLoading = true), allValues[1])
            assertTrue(allValues[2].let {
                !it.isLoading && it.userDetail == userDetail && it.error == null
            })
        }
    }
}
