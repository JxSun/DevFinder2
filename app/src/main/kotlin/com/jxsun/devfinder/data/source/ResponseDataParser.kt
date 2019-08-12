package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.model.exception.ClientException
import com.jxsun.devfinder.model.exception.ServerException
import com.jxsun.devfinder.model.exception.UnknownAccessException
import io.reactivex.Single
import io.reactivex.SingleTransformer
import retrofit2.Response
import timber.log.Timber
import java.util.regex.Pattern

data class ResponseData<T>(
        val nextSinceIdx: Int,
        val data: T?
)

/**
 * The parser to parse the [GitHubService]'s API responses.
 */
class ResponseDataParser<T> {

    /**
     * Parses the API response to check its availability and also transform it to [ResponseData].
     */
    fun parse(): SingleTransformer<Response<T>, ResponseData<T>> {
        return SingleTransformer { upstream ->
            upstream.flatMap { response ->
                // Check http response status
                val httpCode = response.code()
                when {
                    httpCode in 400..499 -> throw ClientException(httpCode)
                    httpCode >= 500 -> throw ServerException(httpCode)
                }
                if (!response.isSuccessful) {
                    throw UnknownAccessException(httpCode, response.errorBody()?.string())
                }

                val link = response.headers().get(LINK_HEADER)
                        .extractLinks()
                        .also {
                            Timber.d("link: next=${it[NEXT_LINK]}")
                        }

                Single.just(ResponseData(
                        nextSinceIdx = link[NEXT_LINK] ?: 0,
                        data = response.body()
                ))
            }
        }
    }

    companion object {
        private val LINK_PATTERN = Pattern.compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private const val LINK_HEADER = "Link"
        private const val NEXT_LINK = "next"

        private fun String?.extractLinks(): Map<String, Int> {
            val links = mutableMapOf<String, Int>()
            val matcher = LINK_PATTERN.matcher(this)

            while (matcher.find()) {
                if (matcher.groupCount() == 2) {
                    val key = matcher.group(2)
                    Timber.v("parsing link key: $key")
                    if (key == NEXT_LINK) {
                        links[key] = try {
                            matcher.group(1)
                                    .also { Timber.v("parsing link url: $it") }
                                    .substringAfter("since=")
                                    .substringBefore("&")
                                    .toInt()
                        } catch (e: Exception) {
                            break
                        }
                    }
                }
            }
            return links
        }
    }
}