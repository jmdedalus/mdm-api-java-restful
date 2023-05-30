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

import spock.lang.Specification
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.terminology.Terminology

class RestfulClientSpec extends Specification {

    final static String URL = 'https://modelcatalogue.cs.ox.ac.uk/continuous-deployment/'
    final static UUID API_KEY = UUID.fromString('b13abe48-d85e-49a9-b6e5-d6506d653d40')

    BindingMauroDataMapperClient getClient() {

        BindingMauroDataMapperClient client = new BindingMauroDataMapperClient(URL, API_KEY)
        return client

    }



    void 'Test retrieving a data model' () {
        when: "Download and bind a data model"
        DataModel dm
        getClient().withCloseable {client ->
            dm = client.findAndExportAndBindDataModelByName("Complex Test DataModel")
        }
        then: "the data model is present and correct"
        dm != null
        dm.childDataClasses.size() == 3
        dm.dataClasses.size() == 4

    }

    void 'Test retrieving a terminology' () {
        when: "Download and bind a terminology"
        Terminology terminology
        getClient().withCloseable {client ->
            terminology = client.findAndExportAndBindTerminologyByName("Complex Test Terminology")
        }
        then: "the terminology is present and correct"
        terminology != null
        terminology.terms.size() == 102
        terminology.termRelationshipTypes.size() == 4
    }

    void 'Test getting folder by name' () {
        when: "Find a folder by name"
        UUID folderId
        List<UUID> dataModelIds
        getClient().withCloseable {client ->
            folderId = client.findFolderByName("SDK Testing")
            dataModelIds = client.listDataModelsInFolder(folderId)
        }
        then:
        folderId.toString() == '2f6d030f-e4e0-408c-a2e8-6989cf6af9a4'
        dataModelIds.size() == 1

    }



}
