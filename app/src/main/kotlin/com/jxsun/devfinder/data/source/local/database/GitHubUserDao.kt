package com.jxsun.devfinder.data.source.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Single

@Dao
interface GitHubUserDao {

    @Query("SELECT * FROM ${AppDatabase.GITHUB_USER_TABLE_NAME}")
    fun getAll(): Single<List<GitHubUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entity: GitHubUserEntity)

    @Transaction
    fun reset(entities: List<GitHubUserEntity>) {
        clear()
        insertAll(*entities.toTypedArray())
    }

    @Query("DELETE FROM ${AppDatabase.GITHUB_USER_TABLE_NAME}")
    fun clear()
}
