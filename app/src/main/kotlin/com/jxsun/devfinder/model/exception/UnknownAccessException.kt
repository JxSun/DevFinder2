package com.jxsun.devfinder.model.exception

/**
 * The exception class to stand for the unknown issues which fail the data accessing task.
 */
data class UnknownAccessException(
    val httpCode: Int?,
    val msg: String? = null
) : Exception("${msg ?: "Failed to access server due to unknown reason"}: HTTP $httpCode")
