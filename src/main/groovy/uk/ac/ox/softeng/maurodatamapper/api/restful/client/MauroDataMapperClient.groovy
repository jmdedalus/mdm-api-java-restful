package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import uk.ac.ox.softeng.maurodatamapper.api.restful.application.LocalMauroDataMapperApplication
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.MauroDataMapperConnection
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.CatalogueItemPrefix
import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.MauroDataMapperEndpoint
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.FileParameter
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.ModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.SummaryMetadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelFileImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.terminology.Terminology
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.web.databinding.DataBinder
import groovy.util.logging.Slf4j
import io.micronaut.core.type.Argument

@Slf4j
@SuppressWarnings('unused')
class MauroDataMapperClient implements DataBinder, Closeable {

    public static String DEFAULT_CONNECTION_NAME = '_default'

    private final Map<String, MauroDataMapperConnection> NAMED_CONNECTIONS = [:]

    protected final String defaultConnectionName

    MauroDataMapperClient(Properties properties) {
        this(DEFAULT_CONNECTION_NAME, properties)
    }

    MauroDataMapperClient(String connectionName, Properties properties) {
        defaultConnectionName = connectionName
        LocalMauroDataMapperApplication.instance.withCloseable {application ->
            application.start()
            openConnection(connectionName, properties)
        }
    }

    MauroDataMapperClient(String baseUrl, String username, String password) {
        this(DEFAULT_CONNECTION_NAME, baseUrl, username, password)
    }

    MauroDataMapperClient(String connectionName, String baseUrl, String username, String password) {
        defaultConnectionName = connectionName
        LocalMauroDataMapperApplication.instance.withCloseable {application ->
            application.start()
            openConnection(connectionName, baseUrl, username, password)
        }
    }

    MauroDataMapperConnection getDefaultConnection() {
        getConnection(defaultConnectionName)
    }

    MauroDataMapperConnection getConnection(String name) {
        NAMED_CONNECTIONS[name]
    }

    void openConnection(String name, Properties properties) {
        closeConnection(name)
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection(properties)
    }

    void openConnection(String name, String baseUrl, String username, String password) {
        closeConnection(name)
        NAMED_CONNECTIONS[name] = new MauroDataMapperConnection(baseUrl, username, password)
    }

    void closeConnection(String name) {
        NAMED_CONNECTIONS.remove(name)?.close()
    }

    @Override
    void close() {
        NAMED_CONNECTIONS.each {k, conn ->
            conn.close()
        }
        application.close()
    }

    void deleteFolder(UUID folderId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.FOLDER_DELETE.build(id: folderId, permanent: permanent)
        )
    }

    void deleteDataModel(UUID dataModelId, boolean permanent = true, String connectionName = defaultConnectionName) {
        getConnection(connectionName).DELETE(
            MauroDataMapperEndpoint.DATAMODEL_DELETE.build(id: dataModelId, permanent: permanent)
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
        ).body().items.collect {Map m -> Utils.toUuid(m.id)}
    }

    List<UUID> listTopLevelFolders(String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDERS_LIST.build()
        ).body().items.collect {Map m -> Utils.toUuid(m.id)}
    }

    UUID createFolder(String folderName, UUID parentFolderId = null, String connectionName = defaultConnectionName) {
        String endpoint = parentFolderId ? MauroDataMapperEndpoint.FOLDER_CREATE.build() :
                          MauroDataMapperEndpoint.FOLDER_FOLDER_CREATE.build(folderId: parentFolderId)
        String id = getConnection(connectionName).POST(endpoint, [label: folderName]).body().id
        Utils.toUuid(id)
    }

    List<UUID> listDataModelsInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_DATAMODELS.build(folderId: folderId)
        ).body().items.collect {Map m -> Utils.toUuid(m.id)}
    }

    List<UUID> listTerminologiesInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_TERMINOLOGIES.build(folderId: folderId)
        ).body().items.collect {Map m -> Utils.toUuid(m.id)}
    }

    List<UUID> listCodeSetsInFolder(UUID folderId, String connectionName = defaultConnectionName) {
        getConnection(connectionName).GET(
            MauroDataMapperEndpoint.FOLDER_LIST_CODESETS.build(folderId: folderId)
        ).body().items.collect {Map m -> Utils.toUuid(m.id)}
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
            application.writeBody(summaryMetadata),
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
        listSummaryMetadata(catalogueItemPrefix, catalogueItemId, connectionName).each {summary ->
            deleteSummaryMetadata(catalogueItemPrefix, catalogueItemId, summary.id as String, connectionName)
        }
    }

    UUID addMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId, Metadata metadata,
                     String connectionName = defaultConnectionName) {
        String id = getConnection(connectionName).POST(
            MauroDataMapperEndpoint.METADATA_CREATE.build(catalogueItemDomainType: catalogueItemPrefix.representation,
                                                          catalogueItemId: catalogueItemId),
            application.writeBody(metadata),
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
        listMetadata(catalogueItemPrefix, catalogueItemId, connectionName).each {metadata ->
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
        listDataModelImporters(connectionName).find {imp -> imp.name == 'JsonImporterService'}
    }

    Map getJsonTerminologyImporterProperties(String connectionName = defaultConnectionName) {
        listDataModelImporters(connectionName).find {imp -> imp.name == 'JsonImporterService'}
    }

    Map getJsonDataModelExporterProperties(String connectionName = defaultConnectionName) {
        listDataModelImporters(connectionName).find {imp -> imp.name == 'JsonExporterService'}
    }

    Map getJsonTerminologyExporterProperties(String connectionName = defaultConnectionName) {
        listDataModelImporters(connectionName).find {imp -> imp.name == 'JsonExporterService'}
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
                          connectionName).dataModel as Map
    }

    Map importDataModel(String importerNamespace, String importerName, String importerVersion,
                        DataModelImporterProviderServiceParameters parameters,
                        String connectionName = defaultConnectionName) {

        getConnection(connectionName).POST(
            MauroDataMapperEndpoint.DATAMODEL_IMPORT.build(importerNamespace: importerNamespace,
                                                           importerName: importerName,
                                                           importerVersion: importerVersion),
            application.writeBody(parameters),
            Argument.of(Map)
        ).body()
    }

    Map importTerminology(String importerNamespace, String importerName, String importerVersion,
                          ModelImporterProviderServiceParameters parameters,
                          String connectionName = defaultConnectionName) {

        getConnection(connectionName).POST(
            MauroDataMapperEndpoint.TERMINOLOGY_IMPORT.build(importerNamespace: importerNamespace,
                                                             importerName: importerName,
                                                             importerVersion: importerVersion),
            application.writeBody(parameters),
            Argument.of(Map)
        ).body()
    }

    UUID importDataModel(DataModel dataModel, UUID folderId, String dataModelName, Boolean finalised, Boolean importAsNewDocumentationVersion,
                         String connectionName = defaultConnectionName) {

        FileParameter fileParameter = new FileParameter("temporaryFile", "",
                                                        application.jsonExporterService.
                                                            exportDataModel(getConnection(connectionName).loggedInUser, dataModel)
                                                            .toByteArray())

        DataModelFileImporterProviderServiceParameters parameters = new DataModelFileImporterProviderServiceParameters(
            folderId: folderId,
            modelName: dataModelName,
            finalised: finalised,
            importAsNewDocumentationVersion: importAsNewDocumentationVersion,
            importFile: fileParameter
        )
        Map importerProperties = getJsonDataModelImporterProperties(connectionName)
        String id = importDataModel(
            importerProperties.namespace as String,
            importerProperties.name as String,
            importerProperties.version as String,
            parameters, connectionName).id
        Utils.toUuid(id)
    }


    UUID importTerminology(Terminology terminology, UUID folderId, String terminologyName, Boolean
        finalised, Boolean importAsNewDocumentationVersion, String connectionName = defaultConnectionName) {

        FileParameter fileParameter = new FileParameter("temporaryFile", "",
                                                        application.xmlTerminologyExporterService
                                                            .exportTerminology(getConnection(connectionName).loggedInUser,
                                                                               terminology)
                                                            .toByteArray())

        ModelImporterProviderServiceParameters parameters = new ModelImporterProviderServiceParameters(
            folderId: folderId,
            modelName: terminologyName,
            finalised: finalised,
            importAsNewDocumentationVersion: importAsNewDocumentationVersion,
            importFile: fileParameter
        )
        Map importerProperties = getJsonTerminologyImporterProperties(connectionName)
        String id = importTerminology(
            importerProperties.namespace as String,
            importerProperties.name as String,
            importerProperties.version as String,
            parameters, connectionName).id
        Utils.toUuid(id)
    }

    UUID findDataModelIdByName(String name, String connectionName = defaultConnectionName) {
        String id = getConnection(connectionName).GET(
            MauroDataMapperEndpoint.DATAMODEL_LIST.build()
        ).body().items.find {
            it.label == name
        }.id
        Utils.toUuid(id)
    }

    UUID findTerminologyIdByName(String name, String connectionName = defaultConnectionName) {
        String id = getConnection(connectionName).GET(
            MauroDataMapperEndpoint.TERMINOLOGY_LIST.build()
        ).body().items.find {
            it.label == name
        }.id
        Utils.toUuid(id)
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

    def updateDataModelDescription(UUID dataModelId, String newDescription, String connectionName = defaultConnectionName) {
        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.DATAMODEL_ID.build(id: dataModelId),
            [description: newDescription]
        ).body()
    }

    def updateDataElementDescription(UUID dataModelId, UUID dataClassId, UUID dataElementId, String newDescription,
                                     String connectionName = defaultConnectionName) {

        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.DATAELEMENT_ID.build(dataModelId: dataModelId,
                                                         dataClassId: dataClassId,
                                                         id: dataElementId),
            [description: newDescription]
        ).body()
    }

    def updateTermDescription(UUID terminologyId, UUID termId, String newDescription, String connectionName = defaultConnectionName) {
        getConnection(connectionName).PUT(
            MauroDataMapperEndpoint.TERM_ID.build(terminologyId: terminologyId,
                                                  id: termId),
            [description: newDescription]
        ).body()
    }

    def updateDataClassDescription(UUID dataModelId, UUID parentClassId, UUID dataClassId, String newDescription,
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


    void copyDataModel(UUID dataModelId, String targetConnectionName, UUID targetFolder, boolean
        importAsNewDocumentationVersion, String connectionName = defaultConnectionName) {
        DataModel dataModel = getDataModelById(dataModelId)
        uploadDataModel(dataModel, targetFolder, dataModel.label, dataModel.finalised, importAsNewDocumentationVersion, targetConnectionName)
    }

    void copyFolder(String sourceConnectionName = "default", UUID folderId, String targetCatalogueName, UUID parentFolderId = null, boolean
        importAsNewDocumentationVersion) {
        Folder sourceFolder = getFolderById(folderId, sourceConnectionName)
        UUID targetFolder = createFolder(sourceFolder.label, parentFolderId, targetCatalogueName)
        List<UUID> dataModelsInSourceFolder = listDataModelsInFolder(folderId, sourceConnectionName)
        dataModelsInSourceFolder.each {sourceDataModelId ->
            copyDataModel(sourceDataModelId, targetCatalogueName, targetFolder, importAsNewDocumentationVersion, sourceConnectionName)
        }
        List<UUID> subFoldersInSourceFolder = listSubFoldersInFolder(folderId, sourceConnectionName)
        subFoldersInSourceFolder.each {sourceSubFolderId ->
            copyFolder(sourceConnectionName, sourceSubFolderId, targetCatalogueName, targetFolder, importAsNewDocumentationVersion)
        }

    }


    UUID getFolderByName(String folderName, UUID parentFolder = null, String connectionName = defaultConnectionName) {


        RestResponse restResponse
        if (parentFolder) {
            restResponse = getConnection(connectionName).GET(MauroDataMapperEndpoint.FOLDER_LIST_FOLDERS, [parentFolder.toString()])
        } else {
            restResponse = getConnection(connectionName).GET(MauroDataMapperEndpoint.FOLDERS_LIST)
        }
        String id = restResponse.json.items.find {it.label.trim() == folderName.trim()}?.id
        if (!id) {
            return null
        }
        return UUID.fromString(id)
    }


    UUID findOrCreateFolderByName(String folderName, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        UUID folderId = getFolderByName(folderName.trim(), parentFolder, connectionName)
        if (!folderId) {
            folderId = createFolder(folderName.trim(), parentFolder, connectionName)
        }
        return folderId
    }

    UUID findOrCreateFolderByPath(String folderPath, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        List<String> folderPathComponents = folderPath.split("\\.")
        return findOrCreateFolderByPath(folderPathComponents, parentFolder, connectionName)
    }

    UUID findOrCreateFolderByPath(List<String> folderPathComponents, UUID parentFolder = null, String connectionName = defaultConnectionName) {
        UUID newParent = parentFolder
        folderPathComponents.each {folderName ->
            newParent = findOrCreateFolderByName(folderName, newParent, connectionName)
        }
        return newParent
    }


}
