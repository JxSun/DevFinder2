package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.data.repository.Mapper
import com.jxsun.devfinder.model.GitHubUser

/**
 * A mapper which provides mappings between [GitHubUser] and [UserResponse].
 */
class RemoteDataMapper : Mapper<GitHubUser, UserResponse> {

    override fun toModel(implData: UserResponse): GitHubUser {
        return GitHubUser(
                id = implData.id,
                loginName = implData.name,
                avatarUrl = implData.avatarUrl,
                siteAdmin = implData.siteAdmin
        )
    }

    override fun fromModel(model: GitHubUser): UserResponse {
        return UserResponse(
                id = model.id,
                name = model.loginName,
                avatarUrl = model.avatarUrl,
                siteAdmin = model.siteAdmin
        )
    }
}