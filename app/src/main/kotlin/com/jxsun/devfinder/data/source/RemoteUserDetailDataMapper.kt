package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.data.repository.Mapper
import com.jxsun.devfinder.model.GitHubUserDetail

/**
 * A mapper which provides mappings between [GitHubUserDetail] and [UserDetailResponse].
 */
class RemoteUserDetailDataMapper : Mapper<GitHubUserDetail, UserDetailResponse> {

    override fun toModel(implData: UserDetailResponse): GitHubUserDetail {
        return GitHubUserDetail(
            id = implData.id,
            loginName = implData.loginName,
            name = implData.name,
            avatarUrl = implData.avatarUrl,
            siteAdmin = implData.siteAdmin,
            blog = implData.blog,
            location = implData.location,
            bio = implData.bio
        )
    }

    override fun fromModel(model: GitHubUserDetail): UserDetailResponse {
        return UserDetailResponse(
            id = model.id,
            name = model.name.orEmpty(),
            loginName = model.loginName,
            avatarUrl = model.avatarUrl,
            siteAdmin = model.siteAdmin,
            blog = model.blog.orEmpty(),
            location = model.location.orEmpty(),
            bio = model.bio.orEmpty()
        )
    }
}
