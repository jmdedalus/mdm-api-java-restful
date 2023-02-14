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
package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import spock.lang.Specification
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel

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


}
