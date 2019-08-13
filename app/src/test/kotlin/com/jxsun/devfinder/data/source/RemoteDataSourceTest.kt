package com.jxsun.devfinder.data.source

import com.jxsun.devfinder.model.exception.NoConnectionException
import com.jxsun.devfinder.util.NetworkChecker
import com.nhaarman.mockitokotlin2.doReturn
import io.reactivex.Single
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RemoteDataSourceTest {

    @Mock
    private lateinit var networkChecker: NetworkChecker

    private lateinit var mockWebServer: MockWebServer

    private lateinit var sut: RemoteDataSource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        mockWebServer = MockWebServer()

        sut = RemoteDataSource(
            gitHubService = GitHubServiceStub(mockWebServer.url("/")),
            networkChecker = networkChecker,
            remoteUserDataMapper = RemoteUserDataMapper(),
            remoteUserDetailDataMapper = RemoteUserDetailDataMapper()
        )
    }

    @Test
    fun `fetch users but has no connection`() {
        doReturn(false).`when`(networkChecker).isNetworkConnected()

        val testObserver = sut.getUsers(since = 10).test()

        testObserver.assertError { it is NoConnectionException }
    }

    @Test
    fun `fetch users successfully`() {
        doReturn(true).`when`(networkChecker).isNetworkConnected()

        mockWebServer.enqueue(
            MockResponse()
                .addHeader(
                    "Link",
                    "<https://api.github.com/users?since=37>; rel=\"next\", <https://api.github.com/users{?since}>; rel=\"first\""
                )
                .setResponseCode(200)
                .setBody(
                    "[\n" +
                            "    {\n" +
                            "        \"login\": \"KirinDave\",\n" +
                            "        \"id\": 36,\n" +
                            "        \"node_id\": \"MDQ6VXNlcjM2\",\n" +
                            "        \"avatar_url\": \"https://avatars2.githubusercontent.com/u/36?v=4\",\n" +
                            "        \"gravatar_id\": \"\",\n" +
                            "        \"url\": \"https://api.github.com/users/KirinDave\",\n" +
                            "        \"html_url\": \"https://github.com/KirinDave\",\n" +
                            "        \"followers_url\": \"https://api.github.com/users/KirinDave/followers\",\n" +
                            "        \"following_url\": \"https://api.github.com/users/KirinDave/following{/other_user}\",\n" +
                            "        \"gists_url\": \"https://api.github.com/users/KirinDave/gists{/gist_id}\",\n" +
                            "        \"starred_url\": \"https://api.github.com/users/KirinDave/starred{/owner}{/repo}\",\n" +
                            "        \"subscriptions_url\": \"https://api.github.com/users/KirinDave/subscriptions\",\n" +
                            "        \"organizations_url\": \"https://api.github.com/users/KirinDave/orgs\",\n" +
                            "        \"repos_url\": \"https://api.github.com/users/KirinDave/repos\",\n" +
                            "        \"events_url\": \"https://api.github.com/users/KirinDave/events{/privacy}\",\n" +
                            "        \"received_events_url\": \"https://api.github.com/users/KirinDave/received_events\",\n" +
                            "        \"type\": \"User\",\n" +
                            "        \"site_admin\": false\n" +
                            "    }" +
                            "]"
                )

        )

        val testObserver = sut.getUsers(since = 35).test()

        testObserver.assertValue {
            it.users.single().loginName == "KirinDave"
        }
        testObserver.assertValue { it.nextSinceIdx == 37 }
    }

    @Test
    fun `fetch user detail but has no connection`() {
        doReturn(false).`when`(networkChecker).isNetworkConnected()

        val testObserver = sut.getUserDetail(login = "Josh").test()

        testObserver.assertError { it is NoConnectionException }
    }

    @Test
    fun `fetch user detail successfully`() {
        doReturn(true).`when`(networkChecker).isNetworkConnected()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    "{\n" +
                            "    \"login\": \"mojombo\",\n" +
                            "    \"id\": 1,\n" +
                            "    \"node_id\": \"MDQ6VXNlcjE=\",\n" +
                            "    \"avatar_url\": \"https://avatars0.githubusercontent.com/u/1?v=4\",\n" +
                            "    \"gravatar_id\": \"\",\n" +
                            "    \"url\": \"https://api.github.com/users/mojombo\",\n" +
                            "    \"html_url\": \"https://github.com/mojombo\",\n" +
                            "    \"followers_url\": \"https://api.github.com/users/mojombo/followers\",\n" +
                            "    \"following_url\": \"https://api.github.com/users/mojombo/following{/other_user}\",\n" +
                            "    \"gists_url\": \"https://api.github.com/users/mojombo/gists{/gist_id}\",\n" +
                            "    \"starred_url\": \"https://api.github.com/users/mojombo/starred{/owner}{/repo}\",\n" +
                            "    \"subscriptions_url\": \"https://api.github.com/users/mojombo/subscriptions\",\n" +
                            "    \"organizations_url\": \"https://api.github.com/users/mojombo/orgs\",\n" +
                            "    \"repos_url\": \"https://api.github.com/users/mojombo/repos\",\n" +
                            "    \"events_url\": \"https://api.github.com/users/mojombo/events{/privacy}\",\n" +
                            "    \"received_events_url\": \"https://api.github.com/users/mojombo/received_events\",\n" +
                            "    \"type\": \"User\",\n" +
                            "    \"site_admin\": false,\n" +
                            "    \"name\": \"Tom Preston-Werner\",\n" +
                            "    \"company\": null,\n" +
                            "    \"blog\": \"http://tom.preston-werner.com\",\n" +
                            "    \"location\": \"San Francisco\",\n" +
                            "    \"email\": null,\n" +
                            "    \"hireable\": null,\n" +
                            "    \"bio\": null,\n" +
                            "    \"public_repos\": 61,\n" +
                            "    \"public_gists\": 62,\n" +
                            "    \"followers\": 21586,\n" +
                            "    \"following\": 11,\n" +
                            "    \"created_at\": \"2007-10-20T05:24:19Z\",\n" +
                            "    \"updated_at\": \"2019-08-07T00:36:33Z\"\n" +
                            "}"
                )
        )

        val testObserver = sut.getUserDetail(login = "mojombo").test()

        testObserver.assertValue {
            it.userDetail.name == "Tom Preston-Werner"
        }
    }
}

class GitHubServiceStub(baseUrl: HttpUrl) : GitHubService {

    private val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(GitHubService::class.java)

    override fun getAllUsers(
        since: Int,
        clientId: String,
        clientSecret: String
    ): Single<Response<List<UserResponse>>> {
        return service.getAllUsers(since, clientId, clientSecret)
    }

    override fun getUser(loginName: String): Single<Response<UserDetailResponse>> {
        return service.getUser(loginName)
    }
}
