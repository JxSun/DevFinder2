package com.jxsun.devfinder.data.source

import com.google.gson.annotations.SerializedName

/**
 * Represents the GitHub user response.
 */
data class UserResponse(
        val id: Long,
        @SerializedName("login") val loginName: String,
        @SerializedName("avatar_url") val avatarUrl: String,
        @SerializedName("site_admin") val siteAdmin: Boolean
)