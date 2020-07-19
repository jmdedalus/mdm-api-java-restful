package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.transform.CompileStatic

@CompileStatic
class ClientUser implements User {

    String firstName
    String lastName
    String emailAddress
    String tempPassword
    UUID id

    @Override
    String getDomainType() {
        ClientUser
    }
}
