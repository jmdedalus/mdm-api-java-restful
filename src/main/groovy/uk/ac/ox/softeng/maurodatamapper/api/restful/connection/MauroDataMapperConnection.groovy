/*
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.api.restful.connection

import io.micronaut.http.ssl.ClientSslConfiguration
import uk.ac.ox.softeng.maurodatamapper.api.restful.client.ClientUser
import uk.ac.ox.softeng.maurodatamapper.api.restful.client.RestClientInterface
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.MauroDataMapperEndpoint
import uk.ac.ox.softeng.maurodatamapper.api.restful.exception.ApiClientException
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.web.databinding.DataBinder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.netty.DefaultHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.netty.cookies.NettyCookie
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder

import java.time.Duration

@Slf4j
//@CompileStatic
class MauroDataMapperConnection implements DataBinder, Closeable, RestClientInterface {

    protected String baseUrl

    ClientUser clientUser

    UUID apiKey

    HttpClient client
    NettyCookie currentCookie

    MauroDataMapperConnection(String baseUrl, String username, String password, Boolean insecureTls = false) {
        setClient(baseUrl, insecureTls)
        login(username, password)
    }

    MauroDataMapperConnection(String baseUrl, UUID apiKey, Boolean insecureTls = false) {
        setClient(baseUrl, insecureTls)
        this.apiKey = apiKey
        getUserDetails()
    }

    // Local only
    MauroDataMapperConnection() {
        getUserDetails()
    }

    void setClient(String baseUrl, Boolean insecureTls) {
        this.baseUrl = baseUrl + "/api/"

        if(insecureTls) log.warn("Insecure TLS is enabled!! Do not use this option when connecting to production instances")

        this.client = new DefaultHttpClient(new URI(this.baseUrl),
                                            new DefaultHttpClientConfiguration().with {
                                                setReadTimeout(Duration.ofMinutes(30))
                                                setReadIdleTimeout(Duration.ofMinutes(30))
                                                setMaxContentLength(1000 * 1024 * 1024)
                                                setSslConfiguration(new ClientSslConfiguration().with{
                                                    enabled = true
                                                    insecureTrustAllCertificates = insecureTls
                                                    it
                                                })
                                                it
                                            })

    }

    @Override
    void close() {
        if(!apiKey) {
            logout()
        }
        if (client) client.close()
    }

    // When we've got an api key, go and find the current user's details
    void getUserDetails() {
        clientUser = new ClientUser()
        clientUser.firstName = "Anonymous"
        clientUser.lastName = "User"
        clientUser.emailAddress = "anonymous.user@example.com"
    }

    @CompileDynamic
    void login(String usernameParam, String passwordParam) throws ApiClientException {
        def response = POST(MauroDataMapperEndpoint.LOGIN.build(), [
            username: usernameParam,
            password: passwordParam
        ])
        if (response.status() == HttpStatus.OK) {
            Map userMap = response.body()
            userMap.id = Utils.toUuid(userMap.id)
            clientUser = new ClientUser(userMap)
        } else {
            throw new ApiClientException('MDMCXX', 'Cannot login user', MauroDataMapperEndpoint.LOGIN.representation, HttpStatus.OK,
                                         response)
        }
    }

    void login(String[] args) {
        if (args.length > 2) {
            String usernameParam = args[0]
            String passwordParam = args[1]
            login(usernameParam, passwordParam)
        }
    }

    boolean isAuthenticatedSession() {
        GET(MauroDataMapperEndpoint.AUTHENTICATED_SESSION.build()).body().authenticatedSession
    }

    boolean logout() {
        DELETE(MauroDataMapperEndpoint.LOGOUT.build())
        currentCookie = null
    }

    @Override
    HttpResponse<Map> GET(String resourceEndpoint) {
        exchange(HttpRequest.GET(resourceEndpoint), Argument.of(Map))
    }

    @Override
    HttpResponse<Map> POST(String resourceEndpoint, Map body) {
        exchange(HttpRequest.POST(resourceEndpoint, body), Argument.of(Map))
    }

    @Override
    HttpResponse<Map> PUT(String resourceEndpoint, Map body) {
        exchange(HttpRequest.PUT(resourceEndpoint, body), Argument.of(Map))
    }

    @Override
    HttpResponse<Map> DELETE(String resourceEndpoint) {
        exchange(HttpRequest.DELETE(resourceEndpoint), Argument.of(Map))
    }

    @Override
    HttpResponse<Map> DELETE(String resourceEndpoint, Map body) {
        exchange(HttpRequest.DELETE(resourceEndpoint, body), Argument.of(Map))
    }

    @Override
    def <O> HttpResponse<O> GET(String resourceEndpoint, Argument<O> responseBodyType) {
        exchange(HttpRequest.GET(resourceEndpoint), responseBodyType)
    }

    @Override
    def <I, O> HttpResponse<O> POST(String resourceEndpoint, I body, Argument<O> responseBodyType) {
        exchange(HttpRequest.POST(resourceEndpoint, body), responseBodyType)
    }

    @Override
    def <I, O> HttpResponse<O> PUT(String resourceEndpoint, I body, Argument<O> responseBodyType) {
        exchange(HttpRequest.PUT(resourceEndpoint, body), responseBodyType)
    }

    @Override
    def <O> HttpResponse<O> DELETE(String resourceEndpoint, Argument<O> responseBodyType) {
        exchange(HttpRequest.DELETE(resourceEndpoint), responseBodyType)
    }

    @Override
    def <I, O> HttpResponse<O> DELETE(String resourceEndpoint, I body, Argument<O> responseBodyType) {
        exchange(HttpRequest.DELETE(resourceEndpoint, body), responseBodyType)
    }

    private <B> HttpResponse<B> exchange(MutableHttpRequest request, Argument<B> bodyType) {
        try {
            // IIf there's a cookie saved then add it to the request

            if(apiKey) {
                request.header("apiKey", apiKey.toString())
            } else if (currentCookie) {
                request.cookie(currentCookie)
            }

            //TODO we should stop "blocking'
            HttpResponse<B> response = client.toBlocking().exchange(request, bodyType)

            // If we're not sending an apiKey
            // Preserve the JSESSIONID cookie returned from the server
            if (!apiKey && response.header(HttpHeaderNames.SET_COOKIE)) {
                Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(response.header(HttpHeaderNames.SET_COOKIE))
                if (cookies.find { it.name() == 'JSESSIONID' }) currentCookie = new NettyCookie(cookies.find { it.name() == 'JSESSIONID' })
            }
            response

        } catch (HttpClientResponseException responseException) {
            return responseException.response as HttpResponse<B>
        }
    }
}
