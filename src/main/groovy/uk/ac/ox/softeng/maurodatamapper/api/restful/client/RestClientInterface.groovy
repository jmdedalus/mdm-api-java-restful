/*
 * Copyright 2020-2024 University of Oxford and NHS England
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
package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import groovy.transform.CompileStatic
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpResponse
import io.micronaut.http.netty.cookies.NettyCookie

@CompileStatic
interface RestClientInterface {

    NettyCookie getCurrentCookie()

    void setCurrentCookie(NettyCookie currentCookie)

    def <O> HttpResponse<O> GET(String resourceEndpoint)

    def <O> HttpResponse<O> POST(String resourceEndpoint, Map body)

    def <O> HttpResponse<O> PUT(String resourceEndpoint, Map body)

    def <O> HttpResponse<O> DELETE(String resourceEndpoint)

    def <O> HttpResponse<O> DELETE(String resourceEndpoint, Map body)

    def <O> HttpResponse<O> GET(String resourceEndpoint, Argument<O> responseBodyType)

    def <I, O> HttpResponse<O> POST(String resourceEndpoint, I body, Argument<O> responseBodyType)

    def <I, O> HttpResponse<O> PUT(String resourceEndpoint, I body, Argument<O> responseBodyType)

    def <O> HttpResponse<O> DELETE(String resourceEndpoint, Argument<O> responseBodyType)

    def <I, O> HttpResponse<O> DELETE(String resourceEndpoint, I body, Argument<O> responseBodyType)
}
