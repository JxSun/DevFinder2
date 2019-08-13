package com.jxsun.devfinder.model

/**
 * The logic representation of a GitHub user's detailed information.
 */
data class GitHubUserDetail(
    val id: Long,
    val loginName: String,
    val name: String?,
    val avatarUrl: String,
    val siteAdmin: Boolean,
    val blog: String?,
    val location: String?,
    val bio: String?
)
