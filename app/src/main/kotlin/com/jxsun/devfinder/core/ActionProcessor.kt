package com.jxsun.devfinder.core

import io.reactivex.ObservableTransformer

/**
 * The business logic to process the given [Action] and produce a [Result] back.
 */
interface ActionProcessor<A, R> where A : Action, R : Result {

    /**
     * Entry point of processing logic.
     */
    val process: ObservableTransformer<A, R>
}