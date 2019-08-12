package com.jxsun.devfinder.feature.devlist

import com.jxsun.devfinder.core.ActionProcessor
import com.jxsun.devfinder.data.repository.GitHubUserRepository
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber

/**
 * The business logic to process the input [DevListAction]s and accordingly outputs the corresponding
 * [DevListResult]s.
 */
class DevListActionProcessor(
        private val gitHubUserRepository: GitHubUserRepository
) : ActionProcessor<DevListAction, DevListResult> {

    override val process: ObservableTransformer<DevListAction, DevListResult>
        get() {
            return ObservableTransformer { actions ->
                actions.publish { shared ->
                    Observable.merge(
                            shared.ofType(DevListAction.LoadUsersAction::class.java)
                                    .compose(processLoadUsersAction),
                            shared.filter {
                                it !is DevListAction.LoadUsersAction
                            }.flatMap {
                                Observable.error<DevListResult>(
                                        IllegalStateException("Unknown action type: $it")
                                )
                            }
                    )
                }
            }
        }

    private val processLoadUsersAction =
            ObservableTransformer<DevListAction.LoadUsersAction, DevListResult.LoadUsersResult> { upstream ->
                upstream.flatMap<DevListResult.LoadUsersResult> { action ->
                    Timber.v("processor: fetch index since ${action.sinceIndex}")
                    gitHubUserRepository.fetchUsers(since = action.sinceIndex)
                            .toObservable()
                            .map {
                                DevListResult.LoadUsersResult.success(
                                        devList = it.users,
                                        nextSinceIdx = it.nextSinceIdx
                                )
                            }
                            .onErrorResumeNext { error: Throwable ->
                                Observable.just(DevListResult.LoadUsersResult.failure(error))
                            }
                            .startWith(DevListResult.LoadUsersResult.inFlight())
                }
            }
}