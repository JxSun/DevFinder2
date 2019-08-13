package com.jxsun.devfinder.base

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.base.core.Action
import com.jxsun.devfinder.base.core.ActionProcessor
import com.jxsun.devfinder.base.core.Result
import com.jxsun.devfinder.base.core.UiEvent
import com.jxsun.devfinder.base.core.UiState
import com.jxsun.devfinder.base.core.ViewModelContract
import com.jxsun.devfinder.util.extension.plusAssign
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

abstract class BaseViewModel<E : UiEvent, A : Action, R : Result, S : UiState>(
    protected val actionProcessor: ActionProcessor<A, R>
) : ViewModel(), ViewModelContract<E, S> {

    private val uiEventSubject = PublishSubject.create<E>()
    private val disposables = CompositeDisposable()

    protected lateinit var _state: MutableLiveData<S>

    abstract var idleState: S

    /**
     * @see ViewModelContract
     */
    override fun fireEvent(event: E) {
        uiEventSubject.onNext(event)
    }

    /**
     * @see ViewModelContract
     */
    override val state: LiveData<S>
        get() {
            if (!::_state.isInitialized) {
                Timber.d("${this.javaClass.simpleName} setup state binding")
                _state = MutableLiveData()

                // setup MVI rx stream
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

    @CallSuper
    override fun onCleared() {
        Timber.d("${this.javaClass.simpleName} cleared")
        super.onCleared()
        disposables.clear()
    }

    /**
     * Filters the input [UiEvent] to limit its propagation.
     */
    protected abstract val uiEventFilter: ObservableTransformer<E, E>

    /**
     * Combines the [Result] with the previous [UiState] to generate a new [UiState] accordingly.
     */
    protected abstract val uiStateReducer: BiFunction<S, R, S>

    /**
     * Converts a [uiEvent] to an [Action].
     */
    protected abstract fun actionFromUiEvent(uiEvent: E): A
}
