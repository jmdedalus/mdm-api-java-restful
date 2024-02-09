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
package uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint

import groovy.transform.CompileStatic
import io.micronaut.http.uri.UriBuilder

@CompileStatic
enum MauroDataMapperEndpoint {

    LOGIN('authentication/login'),
    AUTHENTICATED_SESSION('/session/isAuthenticated'),
    LOGOUT('authentication/logout'),

    DATAMODEL_IMPORTERS('dataModels/providers/importers'),
    DATAMODEL_EXPORTERS('dataModels/providers/exporters'),

    TERMINOLOGY_IMPORTERS('terminologies/providers/importers'),
    TERMINOLOGY_EXPORTERS('terminologies/providers/exporters'),

    CODESET_IMPORTERS('codeSets/providers/importers'),
    CODESET_EXPORTERS('codeSets/providers/exporters'),

    DATAMODEL_IMPORT('dataModels/import/{importerNamespace}/{importerName}/{importerVersion}'),
    TERMINOLOGY_IMPORT('terminologies/import/{importerNamespace}/{importerName}/{importerVersion}'),
    CODESET_IMPORT('codeSets/import/{importerNamespace}/{importerName}/{importerVersion}'),

    SUMMARY_METADATA_LIST('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata?all=true'),
    SUMMARY_METADATA_CREATE('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata'),
    SUMMARY_METADATA_ID('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata/{id}'),

    METADATA_LIST('{catalogueItemDomainType}/{catalogueItemId}/metadata?all=true'),
    METADATA_CREATE('{catalogueItemDomainType}/{catalogueItemId}/metadata'),
    METADATA_ID('{catalogueItemDomainType}/{catalogueItemId}/metadata/{id}'),

    FOLDER_CREATE('folders'),
    FOLDERS_LIST('folders/?all=true'),

    FOLDER_ID('folders/{id}'),
    FOLDER_DELETE("folders/{id}?permanent={permanent}"),
    FOLDER_FOLDER_CREATE("folders/{folderId}/folders"),

    FOLDER_LIST_FOLDERS("folders/{folderId}/folders?all=true"),
    FOLDER_LIST_DATAMODELS("folders/{folderId}/dataModels?all=true"),
    FOLDER_LIST_TERMINOLOGIES("folders/{folderId}/terminologies?all=true"),
    FOLDER_LIST_CODESETS("folders/{folderId}/codeSets?all=true"),

    DATAMODEL_LIST('dataModels?all=true'),
    DATAMODEL_ID('dataModels/{id}'),
    DATAMODEL_DELETE("dataModels/{dataModelId}?permanent={permanent}"),
    DATAMODEL_EXPORT("dataModels/{dataModelId}/export/{exporterNamespace}/{exporterName}/{exporterVersion}"),

    DATACLASSES("dataModels/{dataModelId}/dataClasses"),
    DATACLASS_ID("dataModels/{dataModelId}/dataClasses/{id}"),
    DATACLASS_DATACLASS_ID("dataModels/{dataModelId}/dataClasses/{dataClassId}/dataClasses/{id}"),
    DATACLASS_DATACLASSES("dataModels/{dataModelId}/dataClasses/{dataClassId}/dataClasses"),
    DATAELEMENT_ID("dataModels/{dataModelId}/dataClasses/{dataClassId}/dataElements/{id}"),

    DATATYPE_ID("dataModels/{dataModelId}/dataTypes/{dataTypeId}"),

    TERMINOLOGY_LIST('terminologies?all=true'),
    TERMINOLOGY_ID('terminologies/{id}'),
    TERMINOLOGY_DELETE("terminologies/{terminologyId}?permanent={permanent}"),
    TERMINOLOGY_EXPORT("terminologies/{terminologyId}/export/{exporterNamespace}/{exporterName}/{exporterVersion}"),

    TERM_ID("terminologies/{terminologyId}/terms/{termId}"),

    CODESET_LIST('codeSets?all=true'),
    CODESET_ID('codeSets/{id}'),
    CODESET_DELETE("codeSets/{id}?permanent={permanent}"),
    CODESET_EXPORT("codeSets/{codeSetId}/export/{exporterNamespace}/{exporterName}/{exporterVersion}"),

    DATAELEMENT_SEMANTIC_LINKS('dataElement/{id}/semanticLinks'),
    DATACLASS_SEMANTIC_LINKS('dataClass/{id}/semanticLinks'),

    DATAFLOW('dataModels/{id}/dataFlows'),
    DATAFLOW_DATACLASSCOMPONENT('dataModels/{dataModelId}/dataFlows/{dataFlowId}/dataClassComponents'),
    DATAFLOW_DATAELEMENTCOMPONENT('dataModels/{dataModelId}/dataFlows/{dataFlowId}/dataClassComponents/{dataClassComponentId}/dataElementComponents'),

    DATACLASS_EXTENDS_DATACLASS('dataModels/{sourceDataModelId}/dataClasses/{sourceDataClassId}/extends/{targetDataModelId}/{targetDataClassId}')

    String representation

    MauroDataMapperEndpoint(String representation) {
        this.representation = representation
    }

    String build(Map params = [:]) {
        UriBuilder.of(representation)
            .expand(params)
            .toString()
    }
}