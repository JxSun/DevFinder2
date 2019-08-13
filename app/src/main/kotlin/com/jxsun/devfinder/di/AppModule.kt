package com.jxsun.devfinder.di

import com.jxsun.devfinder.data.repository.GitHubUserRepository
import com.jxsun.devfinder.data.source.GitHubService
import com.jxsun.devfinder.data.source.RemoteDataSource
import com.jxsun.devfinder.data.source.RemoteUserDataMapper
import com.jxsun.devfinder.data.source.RemoteUserDetailDataMapper
import com.jxsun.devfinder.feature.devdetail.DevDetailActionProcessor
import com.jxsun.devfinder.feature.devdetail.DevDetailViewModel
import com.jxsun.devfinder.feature.devlist.DevListActionProcessor
import com.jxsun.devfinder.feature.devlist.DevListViewModel
import com.jxsun.devfinder.util.NetworkChecker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { GitHubService.Factory().create() }
    single { NetworkChecker(androidContext()) }

    single {
        RemoteDataSource(
            gitHubService = get(),
            remoteUserDataMapper = RemoteUserDataMapper(),
            remoteUserDetailDataMapper = RemoteUserDetailDataMapper(),
            networkChecker = get()
        )
    }

    single { GitHubUserRepository(dataSource = get()) }

    single { DevListActionProcessor(gitHubUserRepository = get()) }
    single { DevDetailActionProcessor(gitHubUserRepository = get()) }

    viewModel { DevListViewModel(get()) }
    viewModel { DevDetailViewModel(get()) }
}
