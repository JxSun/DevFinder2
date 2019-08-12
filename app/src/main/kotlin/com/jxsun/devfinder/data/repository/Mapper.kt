package com.jxsun.devfinder.data.repository

/**
 * A mapper which transforms the domain model to the implementation data, and vice versa.
 */
interface Mapper<M, I> {

    /**
     * Transforms the [implData] to the model [M].
     */
    fun toModel(implData: I): M

    /**
     * Transforms the [model] to the implementation data [I].
     */
    fun fromModel(model: M): I
}