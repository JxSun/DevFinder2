package com.jxsun.devfinder.feature.devlist

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.base.BaseViewModel
import com.jxsun.devfinder.base.core.ViewModelContract
import com.jxsun.devfinder.model.GitHubUser
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import timber.log.Timber

/**
 * The [ViewModel] to cooperate with the list page UI.
 */
class DevListViewModel(
    actionProcessor: DevListActionProcessor
) : BaseViewModel<DevListUiEvent, DevListAction, DevListResult, DevListUiState>(actionProcessor),
    ViewModelContract<DevListUiEvent, DevListUiState> {

    // To enable replacing the idle state in unit test.
    @VisibleForTesting
    override var idleState = DevListUiState.IDLE

    /**
     * @see [BaseViewModel]
     */
    override val uiEventFilter = ObservableTransformer<DevListUiEvent, DevListUiEvent> {
        it.publish { shared ->
            Observable.merge(
                shared.ofType(DevListUiEvent.InitialEvent::class.java).take(1),
                shared.filter { event -> event !is DevListUiEvent.InitialEvent }
            )
        }
    }

    /**
     * @see [BaseViewModel]
     */
    override val uiStateReducer =
        BiFunction<DevListUiState, DevListResult, DevListUiState> { prevState, result ->
            Timber.d("result: $result")
            when (result) {
                is DevListResult.LoadUsersResult -> {
                    when {
                        result.isLoading -> prevState.copy(
                            isLoading = true,
                            error = null
                        )
                        result.error != null -> prevState.copy(
                            isLoading = false,
                            error = result.error
                        )
                        else -> prevState.copy(
                            isLoading = false,
                            devList = mergeDevList(prevState.devList, result.devList),
                            nextSinceIdx = result.nextSinceIdx,
                            error = null
                        )
                    }
                }
            }
        }

    /**
     * Merges the previous [DevListUiState]'s user list and the new [DevListResult]'s user list.
     *
     * If these two lists have overlapping, then remove the overlapped ones in the [prevList] and
     * append those in [newList] to [prevList].
     */
    private fun mergeDevList(
        prevList: List<GitHubUser>,
        newList: List<GitHubUser>
    ): List<GitHubUser> {
        return prevList.lastOrNull()?.let {
            val newListStartId = newList.getOrNull(0)?.id ?: -1
            val prevListEndId = prevList.lastOrNull()?.id ?: -1
            val processedPrevList = mutableListOf<GitHubUser>()

            // The two lists have overlapping.
            if (prevListEndId >= newListStartId) {
                // Find the index starting to be cleared up.
                val startIdxForRemove = prevList.indexOfFirst { it.id >= newListStartId }
                if (startIdxForRemove != -1 && // The index exists.
                    startIdxForRemove != 0 // prevList has more than 1 element.
                ) {
                    // Put the pruned prevList into processedPrevList.
                    processedPrevList.addAll(prevList.subList(0, startIdxForRemove))
                }
            } else {
                processedPrevList.addAll(prevList)
            }
            processedPrevList.apply {
                addAll(newList)
            }
        } ?: newList
    }

    /**
     * @see [BaseViewModel]
     */
    override fun actionFromUiEvent(uiEvent: DevListUiEvent): DevListAction {
        return when (uiEvent) {
            is DevListUiEvent.InitialEvent -> DevListAction.LoadUsersAction(sinceIndex = 0)
            is DevListUiEvent.LoadMoreEvent -> {
                DevListAction.LoadUsersAction(sinceIndex = _state.value?.nextSinceIdx ?: 0)
            }
        }
    }
}
