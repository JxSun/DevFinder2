package com.jxsun.devfinder.model.exception

/**
 * The exception class to stand for server side issues.
 */
data class ServerException(
    val httpCode: Int
) : Exception("Service has some problems internally: HTTP $httpCode")
