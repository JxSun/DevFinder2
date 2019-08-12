package com.jxsun.devfinder.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.jxsun.devfinder.base.core.UiEvent
import com.jxsun.devfinder.base.core.UiState
import com.jxsun.devfinder.base.core.ViewModelContract

abstract class BaseFragment<E : UiEvent, S : UiState, VM : ViewModelContract<E, S>> : Fragment() {

    /**
     * Binds the view model to the base fragment.
     */
    abstract fun bindViewModel(): VM

    /**
     * Sets up the fragment layout resource.
     */
    @LayoutRes
    abstract fun layoutResource(): Int

    /**
     * Gets called when it's time to initialize UI. It happens when [onActivityCreated] just being called.
     */
    abstract fun onInitUi()

    /**
     * Gets called when it's time to deinitialize UI. It happens just before [onDestroyView].
     */
    abstract fun onDeinitUi()

    /**
     * Uses the [uiState] to render UI.
     */
    abstract fun renderUi(uiState: S)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(layoutResource(), container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // setup state change monitor
        bindViewModel().state.observe(
                viewLifecycleOwner,
                Observer { renderUi(it) }
        )

        onInitUi()
    }

    override fun onDestroyView() {
        onDeinitUi()
        super.onDestroyView()
    }
}