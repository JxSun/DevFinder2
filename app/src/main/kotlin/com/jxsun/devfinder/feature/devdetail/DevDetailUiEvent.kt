package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.base.core.UiEvent

/**
 * The concrete UI events for the detailed info page.
 */
sealed class DevDetailUiEvent : UiEvent {

    /**
     * The getting detailed info event.
     */
    data class GetUserDetailEvent(
        val login: String
    ) : DevDetailUiEvent()
}
