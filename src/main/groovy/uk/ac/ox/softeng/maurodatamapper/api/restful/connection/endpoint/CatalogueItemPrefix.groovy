/*
 * Copyright 2020 University of Oxford
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
package uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint

import groovy.transform.CompileStatic

@CompileStatic
enum CatalogueItemPrefix {

    DATAMODEL('dataModels'),
    DATACLASS('dataClasses'),
    DATAELEMENT('dataElements'),
    PRIMITIVE_TYPE('primitiveTypes'),
    ENUMERATION_TYPE('enumerationTypes'),
    ENUMERATION_VALUE('enumerationValues'),
    REFERENCE_TYPE('referenceTypes'),
    TERMINOLOGY('terminologies'),
    TERM('terms'),
    CODESET('codeSets')

    String representation

    CatalogueItemPrefix(String representation) {
        this.representation = representation
    }
}
