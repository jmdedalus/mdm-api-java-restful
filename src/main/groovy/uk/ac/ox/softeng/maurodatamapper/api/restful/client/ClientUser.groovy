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

    ClientUser(Map map) {
        firstName = map.firstName
        lastName = map.lastName
        emailAddress = map.emailAddress
        tempPassword = map.tempPassword
        id = map.id as UUID
    }

    @Override
    String getDomainType() {
        ClientUser
    }
}
