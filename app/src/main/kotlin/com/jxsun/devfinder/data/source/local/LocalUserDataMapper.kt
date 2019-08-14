package com.jxsun.devfinder.data.source.local

import com.jxsun.devfinder.data.repository.Mapper
import com.jxsun.devfinder.data.source.local.database.GitHubUserEntity
import com.jxsun.devfinder.model.GitHubUser

/**
 * A mapper which provides mappings between [GitHubUser] and [GitHubUserEntity].
 */
class LocalUserDataMapper : Mapper<GitHubUser, GitHubUserEntity> {

    override fun toModel(implData: GitHubUserEntity): GitHubUser {
        return GitHubUser(
            id = implData.id,
            loginName = implData.loginName,
            avatarUrl = implData.avatarUrl,
            siteAdmin = implData.siteAdmin
        )
    }

    override fun fromModel(model: GitHubUser): GitHubUserEntity {
        return GitHubUserEntity(
            id = model.id,
            loginName = model.loginName,
            avatarUrl = model.avatarUrl,
            siteAdmin = model.siteAdmin
        )
    }
}
