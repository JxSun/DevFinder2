package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.base.core.Action

/**
 * The concrete business logic action for the detailed info page.
 */
sealed class DevDetailAction : Action {

    /**
     * Gets the user's detailed info by the given [login].
     */
    data class GetUserDetailAction(
        val login: String
    ) : DevDetailAction()
}
