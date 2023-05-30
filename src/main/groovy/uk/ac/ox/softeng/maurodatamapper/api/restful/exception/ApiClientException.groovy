/*
 * Copyright 2020-2023 University of Oxford and NHS England
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
package uk.ac.ox.softeng.maurodatamapper.api.restful.exception

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException

import groovy.transform.CompileStatic
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@CompileStatic
class ApiClientException extends ApiException {

    String endpoint
    HttpStatus expectedStatus
    HttpResponse response


    ApiClientException(String errorCode, String message, String endpoint, HttpStatus expectedStatus, HttpResponse response) {
        super(errorCode, message)
        this.endpoint = endpoint
        this.expectedStatus = expectedStatus
        this.response = response

    }

    @Override
    String getMessage() {
        StringBuffer sb = new StringBuffer(super.getMessage())
        sb.append("Endpoint: ${endpoint}\n")
        sb.append("Actual Status: ${response.status()}(${status})\n")
        sb.append("Message body: ${response.body()}\n")
        return sb.toString()
    }
}
