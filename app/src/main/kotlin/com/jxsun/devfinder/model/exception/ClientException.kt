package com.jxsun.devfinder.model.exception

/**
 * The exception class to stand for client side issues.
 */
data class ClientException(
    val httpCode: Int
) : Exception("Client failed to reach server: HTTP $httpCode")
