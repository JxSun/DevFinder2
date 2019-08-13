package com.jxsun.devfinder.data.source

import com.google.gson.annotations.SerializedName

/**
 * Represents the GitHub user's detailed info response.
 */
data class UserDetailResponse(
    val id: Long,
    val name: String,
    @SerializedName("login") val loginName: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("site_admin") val siteAdmin: Boolean,
    val blog: String,
    val location: String,
    val bio: String
)
