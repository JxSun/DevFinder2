package com.jxsun.devfinder.feature.devdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.base.core.ViewModelContract
import com.jxsun.devfinder.util.extension.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * The [ViewModel] to cooperate with the detailed info page UI.
 */
class DevDetailViewModel(
        private val actionProcessor: DevDetailActionProcessor
) : ViewModelContract<DevDetailUiEvent, DevDetailUiState>() {

    private val uiEventSubject = PublishSubject.create<DevDetailUiEvent>()
    private val disposables = CompositeDisposable()

    private lateinit var _state: MutableLiveData<DevDetailUiState>

    override fun fireEvent(event: DevDetailUiEvent) {
        uiEventSubject.onNext(event)
    }

    override val state: LiveData<DevDetailUiState>
        get() {
            if (!::_state.isInitialized) {
                Timber.d("setup state binding")
                _state = MutableLiveData()

                disposables += uiEventSubject
                        .map(this::actionFromUiEvent)
                        .compose(actionProcessor.process)
                        .scan(DevDetailUiState.IDLE, uiStateReducer)
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
        Timber.d("DevDetailViewModel cleared")
        super.onCleared()
        disposables.clear()
    }

    /**
     * Updates the UI state by referencing the latest result.
     */
    private val uiStateReducer =
            BiFunction<DevDetailUiState, DevDetailResult, DevDetailUiState> { prevState, result ->
                Timber.d("result: $result")
                when (result) {
                    is DevDetailResult.GetUserDetailResult -> {
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
                                    userDetail = result.userDetail,
                                    error = null
                            )
                        }
                    }
                }
            }

    /**
     * Maps the input UI events to actions.
     */
    private fun actionFromUiEvent(uiEvent: DevDetailUiEvent): DevDetailAction {
        return DevDetailAction.GetUserDetailAction(
                login = (uiEvent as DevDetailUiEvent.GetUserDetailEvent).login
        )
    }
}