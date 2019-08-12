package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.base.core.Action

/**
 * The concrete business logic action for the list page.
 */
sealed class DevListAction : Action {

    /**
     * Loads users list starting since the target index.
     */
    data class LoadUsersAction(
            val sinceIndex: Int
    ) : DevListAction()
}