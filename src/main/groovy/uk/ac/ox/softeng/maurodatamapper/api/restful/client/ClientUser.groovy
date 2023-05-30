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
package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import uk.ac.ox.softeng.maurodatamapper.path.Path
import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.transform.CompileStatic
import org.apache.commons.lang3.NotImplementedException

@CompileStatic
class ClientUser implements User {

    String firstName
    String lastName
    String emailAddress
    String tempPassword
    UUID id
    boolean pending
    boolean disabled
    String createdBy

    ClientUser() {

    }

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

    UUID setId(String id) {
        this.id = UUID.fromString(id)
    }

    Path getPath() {
        throw new NotImplementedException('getPath')
    }
}
