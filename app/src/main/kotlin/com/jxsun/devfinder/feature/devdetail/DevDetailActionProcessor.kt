package com.jxsun.devfinder.feature.devdetail

import com.jxsun.devfinder.base.core.ActionProcessor
import com.jxsun.devfinder.data.repository.GitHubUserRepository
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber

/**
 * The business logic to process the input [DevDetailAction]s and accordingly outputs the corresponding
 * [DevDetailResult]s.
 */
class DevDetailActionProcessor(
    private val gitHubUserRepository: GitHubUserRepository
) : ActionProcessor<DevDetailAction, DevDetailResult> {

    override val process: ObservableTransformer<DevDetailAction, DevDetailResult>
        get() {
            return ObservableTransformer { actions ->
                actions.publish { shared ->
                    Observable.merge(
                        shared.ofType(DevDetailAction.GetUserDetailAction::class.java)
                            .compose(processLoadUsersAction),
                        shared.filter {
                            it !is DevDetailAction.GetUserDetailAction
                        }.flatMap {
                            Observable.error<DevDetailResult>(
                                IllegalStateException("Unknown action type: $it")
                            )
                        }
                    )
                }
            }
        }

    private val processLoadUsersAction =
        ObservableTransformer<DevDetailAction.GetUserDetailAction, DevDetailResult.GetUserDetailResult> { upstream ->
            upstream.flatMap<DevDetailResult.GetUserDetailResult> { action ->
                Timber.v("processor: fetch by ${action.login}")
                gitHubUserRepository.fetchUserDetail(login = action.login)
                    .toObservable()
                    .map {
                        DevDetailResult.GetUserDetailResult.success(
                            userDetail = it.user
                        )
                    }
                    .onErrorResumeNext { error: Throwable ->
                        Observable.just(DevDetailResult.GetUserDetailResult.failure(error))
                    }
                    .startWith(DevDetailResult.GetUserDetailResult.inFlight())
            }
        }
}
