package com.jxsun.devfinder.feature.devlist

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.core.ViewModelContract
import com.jxsun.devfinder.util.extension.plusAssign
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * The [ViewModel] to cooperate with the list page UI.
 */
class DevListViewModel(
        private val actionProcessor: DevListActionProcessor
) : ViewModel(), ViewModelContract<DevListUiEvent, DevListUiState> {

    private val uiEventSubject = PublishSubject.create<DevListUiEvent>()
    private val disposables = CompositeDisposable()

    private lateinit var _state: MutableLiveData<DevListUiState>

    // To enable replacing the idle state in unit test.
    @VisibleForTesting
    var idleState = DevListUiState.IDLE

    /**
     * Enables the UI to notify [DevListUiEvent]s to the business logic.
     */
    override fun fireEvent(event: DevListUiEvent) {
        uiEventSubject.onNext(event)
    }

    /**
     * The live data to reflect the changes of [DevListUiState]. It internally wraps a Rx stream to
     * receive the processing results from the business logic.
     */
    override val state: LiveData<DevListUiState>
        get() {
            if (!::_state.isInitialized) {
                Timber.d("setup state binding")
                _state = MutableLiveData()

                disposables += uiEventSubject
                        .compose(uiEventFilter)
                        .map(this::actionFromUiEvent)
                        .compose(actionProcessor.process)
                        .scan(idleState, uiStateReducer)
                        .distinctUntilChanged()
                        .replay(1)
                        .autoConnect(0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            _state.value = it
                        }
            }
            return _state
        }

    override fun onCleared() {
        Timber.d("DevListViewModel cleared")
        super.onCleared()
        disposables.clear()
    }

    /**
     * A filter to help ban unwanted [DevListUiEvent].
     */
    private val uiEventFilter = ObservableTransformer<DevListUiEvent, DevListUiEvent> {
        it.publish { shared ->
            Observable.merge(
                    shared.ofType(DevListUiEvent.InitialEvent::class.java).take(1),
                    shared.filter { event -> event !is DevListUiEvent.InitialEvent }
            )
        }
    }

    /**
     * Updates the UI state by referencing the latest result.
     */
    private val uiStateReducer =
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
     * Maps the input UI events to actions.
     */
    private fun actionFromUiEvent(uiEvent: DevListUiEvent): DevListAction {
        return when (uiEvent) {
            is DevListUiEvent.InitialEvent -> DevListAction.LoadUsersAction(sinceIndex = 0)
            is DevListUiEvent.LoadMoreEvent -> {
                DevListAction.LoadUsersAction(sinceIndex = _state.value?.nextSinceIdx ?: 0)
            }
        }
    }
}