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
