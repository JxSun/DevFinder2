package com.jxsun.devfinder.base.core

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

/**
 * The contract which the concrete view models have to follow.
 */
abstract class ViewModelContract<E : UiEvent, S : UiState> : ViewModel() {

    /**
     * Enable UI layer to notify an [event] to the business logic.
     */
    abstract fun fireEvent(event: E)

    /**
     * Populates the UI state to refresh the UI display.
     */
    abstract val state: LiveData<S>
}