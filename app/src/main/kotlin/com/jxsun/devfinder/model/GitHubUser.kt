package com.jxsun.devfinder.model

/**
 * The GitHub user's business logic model representation.
 */
data class GitHubUser(
        val id: Long,
        val loginName: String,
        val avatarUrl: String,
        val siteAdmin: Boolean
)