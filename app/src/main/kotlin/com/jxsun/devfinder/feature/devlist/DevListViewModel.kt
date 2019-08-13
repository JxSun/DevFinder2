package com.jxsun.devfinder.feature.devlist

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.base.BaseViewModel
import com.jxsun.devfinder.base.core.ViewModelContract
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
                            isLoading = true
                        )
                        result.error != null -> prevState.copy(
                            isLoading = false,
                            error = result.error
                        )
                        else -> prevState.copy(
                            isLoading = false,
                            devList = prevState.devList.toMutableList().apply { addAll(result.devList) },
                            nextSinceIdx = result.nextSinceIdx
                        )
                    }
                }
            }
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
