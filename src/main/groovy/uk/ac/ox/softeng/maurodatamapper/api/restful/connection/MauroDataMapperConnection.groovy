package uk.ac.ox.softeng.maurodatamapper.api.restful.connection

import uk.ac.ox.softeng.maurodatamapper.api.restful.client.RestClientInterface
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.MauroDataMapperEndpoint
import uk.ac.ox.softeng.maurodatamapper.api.restful.exception.ApiClientException
import uk.ac.ox.softeng.maurodatamapper.security.User

import grails.web.databinding.DataBinder
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.client.DefaultHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.netty.cookies.NettyCookie
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder

import java.time.Duration

@Slf4j
class MauroDataMapperConnection implements DataBinder, Closeable, RestClientInterface {

    protected String baseUrl

    @Deprecated
    User loggedInUser

    HttpClient client
    NettyCookie currentCookie

    MauroDataMapperConnection(Properties properties) {
        this(properties.getProperty("client.baseUrl"), properties.getProperty("client.username"), properties.getProperty("client.password"))
    }

    MauroDataMapperConnection(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl + "/api/"
        this.client = new DefaultHttpClient(new URL(this.baseUrl),
                                            new DefaultHttpClientConfiguration().with {
                                                setReadTimeout(Duration.ofMinutes(30))
                                                setReadIdleTimeout(Duration.ofMinutes(30))
                                                it
                                            })

        login(username, password)
    }

    @Override
    void close() {
        logout()
        client.close()
    }

    void login(String usernameParam, String passwordParam) throws ApiClientException {
        POST(MauroDataMapperEndpoint.LOGIN.build(), [
            username: usernameParam,
            password: passwordParam
        ])
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

    /*

    HttpResponse<Map> GET(MauroDataMapperEndpoint endpoint, Map<String, ? super Object> params = [:]) {
        GET(endpoint.build(params))
    }

    def <O> HttpResponse<O> GET(MauroDataMapperEndpoint endpoint, Argument<O> bodyType) {
        GET(endpoint.build([:]), bodyType)
    }

    def <O> HttpResponse<O> GET(MauroDataMapperEndpoint endpoint, Map<String, ? super Object> params, Argument<O> bodyType) {
        GET(endpoint.build(params), bodyType)
    }








    HttpResponse<Map> POST(MauroDataMapperEndpoint endpoint, Map body, Map<String, ? super Object> params = [:]) {
        POST(endpoint.build(params), body)
    }

    def <O> HttpResponse<O> POST(MauroDataMapperEndpoint endpoint, O body, Argument<O> bodyType) {
        POST(endpoint.build([:]), bodyType)
    }

    def <O> HttpResponse<O> POST(MauroDataMapperEndpoint endpoint, Map<String, ? super Object> params, Argument<O> bodyType) {
        POST(endpoint.build(params), bodyType)
    }









    def <O> HttpResponse<O> PUT(MauroDataMapperEndpoint endpoint, Map body, Map<String, ? super Object> params = [:], HttpStatus expected =
    HttpStatus.OK) {
        PUT(endpoint.build(params), body)
    }

    def <O> HttpResponse<O> PUT(MauroDataMapperEndpoint endpoint, Map<String, ? super Object> params, Map body) {
        PUT(endpoint, body, params)
    }

    def <O> HttpResponse<O> DELETE(MauroDataMapperEndpoint endpoint, Map<String, ? super Object> params = [:], HttpStatus expected = NO_CONTENT) {
        DELETE(endpoint.build(params))
    }

    def <O> HttpResponse<O> DELETE(MauroDataMapperEndpoint endpoint, HttpStatus expected) {
        DELETE(endpoint, [:], expected)
    }
*/

    private <B> HttpResponse<B> exchange(MutableHttpRequest request, Argument<B> bodyType) {
        try {
            // IIf there's a cookie saved then add it to the request
            if (currentCookie) request.cookie(currentCookie)

            //TODO we should stop "blocking'
            HttpResponse<B> response = client.toBlocking().exchange(request, bodyType)

            // Preserve the JSESSIONID cookie returned from the server
            if (response.header(HttpHeaderNames.SET_COOKIE)) {
                Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(response.header(HttpHeaderNames.SET_COOKIE))
                if (cookies.find {it.name() == 'JSESSIONID'}) currentCookie = new NettyCookie(cookies.find {it.name() == 'JSESSIONID'})
            }
            response

        } catch (HttpClientResponseException responseException) {
            return responseException.response as HttpResponse<B>
        }
    }
}
