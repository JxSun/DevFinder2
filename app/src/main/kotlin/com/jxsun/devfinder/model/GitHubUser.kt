package com.jxsun.devfinder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * The logic representation of a GitHub user.
 */
@Parcelize
data class GitHubUser(
    val id: Long,
    val loginName: String,
    val avatarUrl: String,
    val siteAdmin: Boolean
) : Parcelable
