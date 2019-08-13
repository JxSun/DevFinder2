package com.jxsun.devfinder.feature.devlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jxsun.devfinder.RxImmediateSchedulerRule
import com.jxsun.devfinder.model.GitHubUser
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

class DevListViewModelTest {

    @Rule
    @JvmField
    val schedulers = RxImmediateSchedulerRule()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var actionProcessor: DevListActionProcessor

    @Mock
    lateinit var stateObserver: Observer<DevListUiState>

    private lateinit var sut: DevListViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sut = DevListViewModel(actionProcessor)
    }

    @Test
    fun `process initial UI event`() {
        val user = GitHubUser(id = 10, loginName = "Bill Gates", avatarUrl = "", siteAdmin = true)
        val processedResult = ObservableTransformer<DevListAction, DevListResult> {
            it.flatMap {
                Observable.fromArray(
                    DevListResult.LoadUsersResult(
                        isLoading = true, devList = emptyList(), nextSinceIdx = 0, error = null
                    ),
                    DevListResult.LoadUsersResult(
                        isLoading = false, devList = listOf(user), nextSinceIdx = 50, error = null
                    )
                )
            }
        }
        doReturn(processedResult).`when`(actionProcessor).process

        sut.state.observeForever(stateObserver)

        sut.fireEvent(DevListUiEvent.InitialEvent)

        verify(actionProcessor).process

        argumentCaptor<DevListUiState>().run {
            verify(stateObserver, times(3)).onChanged(capture())

            assertEquals(DevListUiState.IDLE, allValues[0])
            assertEquals(DevListUiState.IDLE.copy(isLoading = true), allValues[1])
            assertTrue(allValues[2].let {
                !it.isLoading && it.devList[0] == user && it.nextSinceIdx == 50 && it.error == null
            })
        }
    }

    @Test
    fun `process load more UI event`() {
        val user1 = GitHubUser(id = 10, loginName = "Bill Gates", avatarUrl = "", siteAdmin = true)
        val user2 =
            GitHubUser(id = 11, loginName = "Melinda Gates", avatarUrl = "", siteAdmin = false)
        val idleState = DevListUiState(
            isLoading = false,
            devList = listOf(user1),
            nextSinceIdx = 11,
            error = null
        )
        val processedResult = ObservableTransformer<DevListAction, DevListResult> {
            it.flatMap {
                Observable.fromArray(
                    // To simulate there's already an user before loading more.
                    DevListResult.LoadUsersResult(
                        isLoading = true, devList = listOf(user1), nextSinceIdx = 11, error = null
                    ),
                    DevListResult.LoadUsersResult(
                        isLoading = false, devList = listOf(user2), nextSinceIdx = 50, error = null
                    )
                )
            }
        }
        doReturn(processedResult).`when`(actionProcessor).process

        sut.idleState = idleState
        sut.state.observeForever(stateObserver)

        sut.fireEvent(DevListUiEvent.LoadMoreEvent)

        verify(actionProcessor).process

        argumentCaptor<DevListUiState>().run {
            verify(stateObserver, times(3)).onChanged(capture())

            assertEquals(idleState, allValues[0])
            assertTrue(allValues[1].let {
                it.isLoading && it.devList[0] == user1 && it.nextSinceIdx == 11 && it.error == null
            })
            assertTrue(allValues[2].let {
                !it.isLoading && it.devList[1] == user2 && it.nextSinceIdx == 50 && it.error == null
            })
        }
    }
}
