package com.jxsun.devfinder.feature.devdetail

import androidx.lifecycle.ViewModel
import com.jxsun.devfinder.base.BaseViewModel
import com.jxsun.devfinder.base.core.ViewModelContract
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import timber.log.Timber

/**
 * The [ViewModel] to cooperate with the detailed info page UI.
 */
class DevDetailViewModel(
    actionProcessor: DevDetailActionProcessor
) : BaseViewModel<
        DevDetailUiEvent, DevDetailAction, DevDetailResult, DevDetailUiState>(actionProcessor),
    ViewModelContract<DevDetailUiEvent, DevDetailUiState> {

    override var idleState = DevDetailUiState.IDLE

    /**
     * @see [BaseViewModel]
     */
    override val uiEventFilter = ObservableTransformer<DevDetailUiEvent, DevDetailUiEvent> {
        it.publish { shared ->
            shared.ofType(DevDetailUiEvent.GetUserDetailEvent::class.java)
                .cast(DevDetailUiEvent::class.java)
                .take(1)
        }
    }

    /**
     * @see [BaseViewModel]
     */
    override val uiStateReducer =
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
     * @see [BaseViewModel]
     */
    override fun actionFromUiEvent(uiEvent: DevDetailUiEvent): DevDetailAction {
        return DevDetailAction.GetUserDetailAction(
            login = (uiEvent as DevDetailUiEvent.GetUserDetailEvent).login
        )
    }
}
