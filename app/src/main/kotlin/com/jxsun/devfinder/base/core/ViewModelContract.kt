package com.jxsun.devfinder.base.core

import androidx.lifecycle.LiveData

/**
 * The contract which the concrete view models have to follow.
 */
interface ViewModelContract<E : UiEvent, S : UiState> {

    /**
     * Enable UI layer to notify an [event] to the business logic.
     */
    fun fireEvent(event: E)

    /**
     * Populates the UI state to refresh the UI display.
     */
    val state: LiveData<S>
}
