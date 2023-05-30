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

import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.MauroDataMapperConnection
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.CatalogueItemPrefix
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.MauroDataMapperEndpoint
import uk.ac.ox.softeng.maurodatamapper.api.restful.exception.ApiClientException
import uk.ac.ox.softeng.maurodatamapper.api.restful.render.json.JsonViewRenderer
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.FileParameter
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.SummaryMetadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.exporter.DataModelJsonExporterService
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelFileImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.terminology.Terminology
import uk.ac.ox.softeng.maurodatamapper.terminology.provider.exporter.TerminologyJsonExporterService
import uk.ac.ox.softeng.maurodatamapper.terminology.provider.importer.parameter.TerminologyFileImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.terminology.provider.importer.parameter.TerminologyImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@Slf4j
@CompileStatic
@SuppressWarnings('unused')
class MauroDataMapperClient implements Closeable {

    public static String DEFAULT_CONNECTION_NAME = '_default'

    private final Map<String, MauroDataMapperConnection> NAMED_CONNECTIONS = [:]

    protected final String defaultConnectionName

    DataModelJsonExporterService dataModelJsonExporterService

    TerminologyJsonExporterService terminologyJsonExporterService

    MauroDataMapperClient(Properties properties) {
        this(DEFAULT_CONNECTION_NAME, properties)
    }

    MauroDataMapperClient(String baseUrl, String username, String password, Boolean insecureTls = false) {
        this(DEFAULT_CONNECTION_NAME, baseUrl, username, password, insecureTls)
    }

    MauroDataMapperClient(String baseUrl, UUID apiKey, Boolean insecureTls = false) {
        this(DEFAULT_CONNECTION_NAME, baseUrl, apiKey, insecureTls)
    }

    MauroDataMapperClient(String connectionName, Properties properties) {
        this(connectionName, properties.getProperty("client.baseUrl"), properties.getProperty("client.username"),
                properties.getProperty("client.password"), Boolean.parseBoolean(properties.getProperty("client.insecure", "false")))
    }

    MauroDataMapperClient(String connectionName, String baseUrl, String username, String password, Boolean insecureTls = false) {
        defaultConnectionName = connectionName
        openConnection(connectionName, baseUrl, username, password, insecureTls)
        initialiseServices()
    }

    MauroDataMapperClient(String connectionName, String baseUrl, UUID apiKey, Boolean insecureTls = false) {
        defaultConnectionName = connectionName
        openConnection(connectionName, baseUrl, apiKey, insecureTls)
        initialiseServices()
    }
    // Local only client
    MauroDataMapperClient(String connectionName = DEFAULT_CONNECTION_NAME) {
        defaultConnectionName = connectionName
        openLocalConnection(connectionName)
        initialiseServices()
    }


    void initialiseServices() {
        JsonViewRenderer.instance.initialise()
        dataModelJsonExporterService = new DataModelJsonExporterService()
        dataModelJsonExporterService.templateEngine = JsonViewRenderer.instance.templateEngine
        terminologyJsonExporterService = new TerminologyJsonExporterService()
        terminologyJsonExporterService.templateEngine = JsonViewRenderer.instance.templateEngine

    }

    MauroDataMapperConnection getDefaultConnection() {
        getConnection(defaultConnectionName)
    }

    MauroDataMapperConnection getConnection() {
        getDefaultConnection()
    }

    MauroDataMapperConnection getConnection(String name) {
        NAMED_CONNECTIONS[name]
    }

    void openConnection(String name, String baseUrl, String username, String password, Boolean insecureTls) {
        closeConnection(name)
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection(baseUrl, username, password, insecureTls)
    }

    void openConnection(String name, String baseUrl, UUID apiKey, Boolean insecureTls) {
        closeConnection(name)
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection(baseUrl, apiKey, insecureTls)
    }

    void openConnection(String name, Properties properties) {
        closeConnection(name)
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection(properties.getProperty("client.baseUrl"), properties.getProperty("client.username"),
                                                                properties.getProperty("client.password"))
    }

    void openLocalConnection(String name) {
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection()
    }



    void closeConnection(String name) {
        NAMED_CONNECTIONS.remove(name)?.close()
    }

    @Override
    void close() {
        NAMED_CONNECTIONS.each { k, conn ->
            conn.close()
        }
    }

    void deleteFolder(UUID folderId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.FOLDER_DELETE.build(id: folderId, permanent: permanent)
        )
    }

    void deleteDataModel(UUID dataModelId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
                MauroDataMapperEndpoint.DATAMODEL_DELETE.build(dataModelId: dataModelId.toString(), permanent: permanent)
        )
    }

    void deleteTerminology(UUID terminologyId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.TERMINOLOGY_DELETE.build(id: terminologyId, permanent: permanent)
        )
    }

    void deleteCodeSet(UUID codeSetId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.CODESET_DELETE.build(id: codeSetId, permanent: permanent)
        )
    }

    List<UUID> listSubFoldersInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_FOLDERS.build(folderId: folderId)
        ).body().items.collect { Map m -> Utils.toUuid(m.id as String) }
    }

    List<UUID> listTopLevelFolders(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDERS_LIST.build()
        ).body().items.collect { Map m -> Utils.toUuid(m.id as String) }
    }

    UUID createFolder(String folderName, UUID parentFolderId = null, String connectionName = defaultConnectionName) {
        createFolder([label: folderName], parentFolderId, connectionName)
    }

    UUID createFolder(Map folderMap, UUID parentFolderId = null, String connectionName = defaultConnectionName) {
        String endpoint = parentFolderId ? MauroDataMapperEndpoint.FOLDER_FOLDER_CREATE.build(folderId: parentFolderId) :
                          MauroDataMapperEndpoint.FOLDER_CREATE.build()
        def body = getConnection(connectionName).POST(endpoint, folderMap).body()
        String id = body.id
        Utils.toUuid(id)
    }

    List<UUID> listDataModelsInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_DATAMODELS.build(folderId: folderId)
        ).body().items.collect { Map m -> Utils.toUuid(m.id as String) }
    }

    List<UUID> listTerminologiesInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_TERMINOLOGIES.build(folderId: folderId)
        ).body().items.collect { Map m -> Utils.toUuid(m.id as String) }
    }

    List<UUID> listCodeSetsInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_CODESETS.build(folderId: folderId)
        ).body().items.collect { Map m -> Utils.toUuid(m.id as String) }
    }

    void deleteAllSubFoldersInFolder(UUID folderId, boolean permanent, String connectionName = defaultConnectionName) {
        listSubFoldersInFolder(folderId, connectionName).each {
            deleteFolder(it, permanent, connectionName)
        }
    }

    void deleteAllDataModelsInFolder(UUID folderId, boolean permanent, String connectionName = defaultConnectionName) {
        listDataModelsInFolder(folderId, connectionName).each {
            deleteDataModel(it, permanent, connectionName)
        }
    }

    UUID addSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId, SummaryMetadata summaryMetadata,
                            String connectionName = defaultConnectionName) {
        String id = getConnection(connectionName).POST(
            MauroDataMapperEndpoint.SUMMARY_METADATA_CREATE.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                                  catalogueItemId: catalogueItemId),
            JsonViewRenderer.instance.renderDomain(summaryMetadata),
            Argument.of(Map)
        ).body().id
        Utils.toUuid(id)
    }

    List<Map> listSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                  String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.SUMMARY_METADATA_LIST.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                                catalogueItemId: catalogueItemId)
        ).body().items as List<Map>
    }

    void deleteSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId, Serializable summaryMetadataId,
                               String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.SUMMARY_METADATA_ID.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                              catalogueItemId: catalogueItemId,
                                                              id: summaryMetadataId)
        )
    }

    void deleteAllSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                  String connectionName = defaultConnectionName) {
        listSummaryMetadata(catalogueItemPrefix, catalogueItemId, connectionName).each { summary ->
            deleteSummaryMetadata(catalogueItemPrefix, catalogueItemId, summary.id as String, connectionName)
        }
    }

    UUID addMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId, Metadata metadata,
                     String connectionName = defaultConnectionName) {
        String id = getConnection(connectionName).POST(
            MauroDataMapperEndpoint.METADATA_CREATE.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                          catalogueItemId: catalogueItemId),
            JsonViewRenderer.instance.renderDomain(metadata),
            Argument.of(Map)
        ).body().id
        Utils.toUuid(id)
    }

    List<Map> listMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                           String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.METADATA_LIST.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                        catalogueItemId: catalogueItemId)
        ).body().items as List<Map>
    }

    void deleteMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId, Serializable metadataId,
                        String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.METADATA_ID.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                      catalogueItemId: catalogueItemId,
                                                      id: metadataId),
            )
    }

    def deleteAllMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                          String connectionName = defaultConnectionName) {
        listMetadata(catalogueItemPrefix, catalogueItemId, connectionName).each { metadata ->
            deleteMetadata(catalogueItemPrefix, catalogueItemId, metadata.id as String, connectionName)
        }
    }

    List<Map> listDataModelImporters(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.DATAMODEL_IMPORTERS.build(), Argument.listOf(Map)).body()
    }

    List<Map> listDataModelExporters(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.DATAMODEL_EXPORTERS.build(), Argument.listOf(Map)).body()
    }

    List<Map> listTerminologyImporters(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.TERMINOLOGY_IMPORTERS.build(), Argument.listOf(Map)).body()
    }

    List<Map> listTerminologyExporters(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.TERMINOLOGY_EXPORTERS.build(), Argument.listOf(Map)).body()
    }

    Map getJsonDataModelImporterProperties(String connectionName = defaultConnectionName) {
        listDataModelImporters(connectionName).find { imp -> imp.name == 'DataModelJsonImporterService' }
    }

    Map getJsonTerminologyImporterProperties(String connectionName = defaultConnectionName) {
        listTerminologyImporters(connectionName).find { imp -> imp.name == 'TerminologyJsonImporterService' }
    }

    Map getJsonDataModelExporterProperties(String connectionName = defaultConnectionName) {
        listDataModelExporters(connectionName).find { imp -> imp.name == 'DataModelJsonExporterService' }
    }

    Map getJsonTerminologyExporterProperties(String connectionName = defaultConnectionName) {
        listTerminologyExporters(connectionName).find { imp -> imp.name == 'TerminologyJsonExporterService' }
    }

    Map exportDataModel(UUID dataModelId, String exporterNamespace, String exporterName, String exporterVersion,
                        String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.DATAMODEL_EXPORT.build(dataModelId: dataModelId,
                                                           exporterNamespace: exporterNamespace,
                                                           exporterName: exporterName,
                                                           exporterVersion: exporterVersion)
        ).body()
    }

    Map exportDataModel(UUID dataModelId, String connectionName = defaultConnectionName) {
        Map exporterProperties = getJsonDataModelExporterProperties(connectionName)
        exportDataModel(dataModelId,
                        exporterProperties.namespace as String,
                        exporterProperties.name as String,
                        exporterProperties.version as String,
                        connectionName).dataModel as Map
    }

    Map exportTerminology(UUID terminologyId, String exporterNamespace, String exporterName, String exporterVersion,
                          String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.TERMINOLOGY_EXPORT.build(terminologyId: terminologyId,
                                                             exporterNamespace: exporterNamespace,
                                                             exporterName: exporterName,
                                                             exporterVersion: exporterVersion)
        ).body()
    }

    Map exportTerminology(UUID terminologyId, String connectionName = defaultConnectionName) {
        Map exporterProperties = getJsonTerminologyExporterProperties(connectionName)

        exportTerminology(terminologyId,
                          exporterProperties.namespace as String,
                          exporterProperties.name as String,
                          exporterProperties.version as String,
                          connectionName).terminology as Map
    }

    Map importDataModel(String importerNamespace, String importerName, String importerVersion,
                        DataModelImporterProviderServiceParameters parameters,
                        String connectionName = defaultConnectionName) {

        def domain = JsonViewRenderer.instance.renderDomain(parameters)


        HttpResponse<Map> response = getConnection(connectionName).POST(
            MauroDataMapperEndpoint.DATAMODEL_IMPORT.build(importerNamespace: importerNamespace,
                                                           importerName: importerName,
                                                           importerVersion: importerVersion),
            domain,
            Argument.of(Map)
        )
        if (response.status() != HttpStatus.CREATED) {
            throw new ApiClientException('CXX', 'Could not import DataModel ', MauroDataMapperEndpoint.DATAMODEL_IMPORT.representation,
                                         HttpStatus.CREATED, response)
        }
        response.body()
    }

    Map importTerminology(String importerNamespace, String importerName, String importerVersion,
                          TerminologyImporterProviderServiceParameters parameters,
                          String connectionName = defaultConnectionName) {

        HttpResponse<Map> response = getConnection(connectionName).POST(
            MauroDataMapperEndpoint.TERMINOLOGY_IMPORT.build(importerNamespace: importerNamespace,
                                                             importerName: importerName,
                                                             importerVersion: importerVersion),
            JsonViewRenderer.instance.renderDomain(parameters),
            Argument.of(Map)
        )
        if (response.status() != HttpStatus.CREATED) {
            throw new ApiClientException('CXX', 'Could not import Terminology', MauroDataMapperEndpoint.TERMINOLOGY_IMPORT.representation,
                                         HttpStatus.CREATED, response)
        }
        response.body()
    }

    UUID importDataModel(DataModel dataModel, UUID folderId, String dataModelName, Boolean finalised, Boolean importAsNewBranchModelVersion,
                         Boolean importAsNewDocumentationVersion,
                         String connectionName = defaultConnectionName) {

        FileParameter fileParameter = new FileParameter("temporaryFile", "",
                                                        dataModelJsonExporterService.exportDataModel(
                                                            getConnection(connectionName).clientUser,
                                                            dataModel,
                                                            [:]
                                                        ).toByteArray())

        DataModelFileImporterProviderServiceParameters parameters = new DataModelFileImporterProviderServiceParameters(
            folderId: folderId,
            modelName: dataModelName,
            finalised: finalised,
            importAsNewBranchModelVersion: importAsNewBranchModelVersion,
            importAsNewDocumentationVersion: importAsNewDocumentationVersion,
            importFile: fileParameter
        )
        Map importerProperties = getJsonDataModelImporterProperties(connectionName)
        Map response = importDataModel(
                importerProperties.namespace as String,
                importerProperties.name as String,
                importerProperties.version as String,
                parameters, connectionName)
        def importedModel = ((List) response.items)[0]
        String id = importedModel["id"]
        Utils.toUuid(id)
    }

    UUID importTerminology(Terminology terminology, UUID folderId, String terminologyName, Boolean finalised,
                           Boolean importAsNewDocumentationVersion, String connectionName = defaultConnectionName) {

        FileParameter fileParameter = new FileParameter("temporaryFile", "",
                                                        terminologyJsonExporterService.exportTerminology(
                                                            getConnection(connectionName).clientUser,
                                                            terminology,
                                                            [:]
                                                        ).toByteArray())
        TerminologyFileImporterProviderServiceParameters parameters = new TerminologyFileImporterProviderServiceParameters(
            folderId: folderId,
            modelName: terminologyName,
            finalised: finalised,
            importAsNewDocumentationVersion: importAsNewDocumentationVersion,
            importFile: fileParameter
        )
        Map importerProperties = getJsonTerminologyImporterProperties(connectionName)
        def result = importTerminology(
                importerProperties.namespace as String,
                importerProperties.name as String,
                importerProperties.version as String,
                parameters, connectionName)
        Utils.toUuid(((Map)((List)result.items)[0]).id.toString())
    }

    UUID findDataModelIdByName(String name, String connectionName = defaultConnectionName) {
        Map dataModel = getConnection(connectionName).GET(
            MauroDataMapperEndpoint.DATAMODEL_LIST.build()
        ).body().items.find { Map m ->
            isLabelMatch(m.label as String, name)
        } as Map
        if(dataModel) {
            return Utils.toUuid(dataModel.id as String)
        } else return null
    }

    UUID findTerminologyIdByName(String name, String connectionName = defaultConnectionName) {
        Map terminology = getConnection(connectionName).GET(
            MauroDataMapperEndpoint.TERMINOLOGY_LIST.build()
        ).body().items.find { Map m ->
            isLabelMatch(m.label as String, name)
        } as Map
        if(terminology) {
            return Utils.toUuid(terminology.id as String)
        } else return null
    }

    UUID findFolderByName(String name, UUID parentFolderId = null, String connectionName = defaultConnectionName) {
        String endpoint = parentFolderId ? MauroDataMapperEndpoint.FOLDER_LIST_FOLDERS.build(folderId: parentFolderId) :
                          MauroDataMapperEndpoint.FOLDERS_LIST.build()
        Map folder = getConnection(connectionName).GET(endpoint).body().items.find { Map m ->
            isLabelMatch(m.label as String, name)
        } as Map
        if(folder) {
            return Utils.toUuid(folder.id as String)
        } else return null
    }

    Map getFolderById(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.FOLDER_ID.build(id: folderId)).body()
    }

    Map getDataModelById(UUID dataModelId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.DATAMODEL_ID.build(id: dataModelId)).body()
    }

    Map findDataModelByName(String dataModelName, String connectionName = defaultConnectionName) {
        UUID dataModelId = findDataModelIdByName(dataModelName, connectionName)
        getDataModelById(dataModelId, connectionName)
    }

    Map getTerminologyById(UUID terminologyId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(MauroDataMapperEndpoint.TERMINOLOGY_ID.build(id: terminologyId)).body()
    }

    Map findTerminologyByName(String terminologyName, String connectionName = defaultConnectionName) {
        UUID terminologyId = findTerminologyIdByName(terminologyName, connectionName)
        getTerminologyById(terminologyId, connectionName)
    }

    Map updateDataModelDescription(DataModel dataModel, String newDescription, String connectionName = defaultConnectionName) {
        updateDataModelDescription(dataModel.id, newDescription, connectionName)
    }

    Map updateDataModelDescription(UUID dataModelId, String newDescription, String connectionName = defaultConnectionName) {
        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.DATAMODEL_ID.build(id: dataModelId),
            [description: newDescription]
        ).body()
    }

    Map updateDataElementDescription(UUID dataModelId, UUID dataClassId, UUID dataElementId, String newDescription,
                                     String connectionName = defaultConnectionName) {

        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.DATAELEMENT_ID.build(dataModelId: dataModelId,
                                                         dataClassId: dataClassId,
                                                         id: dataElementId),
            [description: newDescription]
        ).body()
    }

    Map updateTermDescription(UUID terminologyId, UUID termId, String newDescription, String connectionName = defaultConnectionName) {
        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.TERM_ID.build(terminologyId: terminologyId,
                                                  id: termId),
            [description: newDescription]
        ).body()
    }

    Map updateDataClassDescription(UUID dataModelId, UUID parentClassId, UUID dataClassId, String newDescription,
                                   String connectionName = defaultConnectionName) {
        String endpoint = parentClassId ? MauroDataMapperEndpoint.DATACLASS_DATACLASS_ID.build(dataModelId: dataModelId,
                                                                                               dataClassId: parentClassId,
                                                                                               id: dataClassId) :
                          MauroDataMapperEndpoint.DATACLASS_ID.build(dataModelId: dataModelId,
                                                                     id: dataClassId)
        getConnection(connectionName).PUT(
            endpoint,
            [description: newDescription]
        ).body()
    }

    UUID findOrCreateFolderByName(String name, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        UUID folderId = findFolderByName(name, parentFolder, connectionName)
        if (folderId) {
            return folderId
        }
        createFolder(name.trim(), parentFolder, connectionName)
    }

    UUID findOrCreateFolderByPath(String folderPath, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        String[] folderPathComponents = folderPath.split("\\.")
        findOrCreateFolderByPath(folderPathComponents.toList(), parentFolder, connectionName)
    }

    UUID findOrCreateFolderByPath(List<String> folderPathComponents, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        UUID newParent = parentFolder
        folderPathComponents.each { folderName ->
            newParent = findOrCreateFolderByName(folderName, newParent, connectionName)
        }
        newParent
    }

    private static boolean isLabelMatch(String left, String right) {
        left.trim() == right.trim()
    }
}
