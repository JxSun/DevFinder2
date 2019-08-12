package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.core.UiEvent

/**
 * The concrete UI events for the list page.
 */
sealed class DevListUiEvent : UiEvent {

    /**
     * UI initialization event.
     */
    object InitialEvent : DevListUiEvent()


    /**
     * Loads more users data.
     */
    object LoadMoreEvent: DevListUiEvent()
}