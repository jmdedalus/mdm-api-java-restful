package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.CatalogueItemPrefix
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.SummaryMetadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.JsonImporterService
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.web.databinding.DataBinder
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings('unused')
class BindingMauroDataMapperClient extends MauroDataMapperClient implements DataBinder {

    JsonImporterService dataModelJsonImporterService

    BindingMauroDataMapperClient(Properties properties) {
        super(properties)
    }

    BindingMauroDataMapperClient(String connectionName, Properties properties) {
        super(connectionName, properties)
    }

    BindingMauroDataMapperClient(String baseUrl, String username, String password) {
        super(baseUrl, username, password)
    }

    BindingMauroDataMapperClient(String connectionName, String baseUrl, String username, String password) {
        super(connectionName, baseUrl, username, password)
    }

    Folder getAndBindFolderById(UUID folderId, String connectionName = defaultConnectionName) {
        Map folderMap = getFolderById(folderId, connectionName)
        Folder folder = new Folder()
        bindData folder, folderMap
        folder
    }

    DataModel exportAndBindDataModelById(UUID id, String connectionName = defaultConnectionName) {
        Map exportModel = exportDataModel(id, connectionName)
        dataModelJsonImporterService.bindMapToDataModel(getConnection(connectionName).clientUser, exportModel.dataModel as Map)
    }

    DataModel findAndExportAndBindDataModelByName(String name, String connectionName = defaultConnectionName) {
        UUID dataModelId = findDataModelIdByName(name, connectionName)
        if (!dataModelId) return null
        exportAndBindDataModelById(dataModelId, connectionName)
    }

    List<SummaryMetadata> listAndBindSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                                     String connectionName = defaultConnectionName) {
        listSummaryMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect {summary ->
            SummaryMetadata summaryMetadata = new SummaryMetadata()
            bindData summaryMetadata, summary
            summaryMetadata.id = Utils.toUuid(summary.id as String)
        } as List<SummaryMetadata>
    }

    List<Metadata> listAndBindMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                       String connectionName = defaultConnectionName) {
        listMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect {md ->
            Metadata metadata = new Metadata()
            bindData metadata, md
            metadata.id = Utils.toUuid(md.id as String)
        } as List<Metadata>
    }

    void copyDataModelToTarget(UUID dataModelId, String targetConnectionName, UUID targetFolderId, boolean importAsNewDocumentationVersion,
                               String sourceConnectionName = defaultConnectionName) {
        DataModel dataModel = exportAndBindDataModelById(dataModelId, sourceConnectionName)
        importDataModel(dataModel, targetFolderId, dataModel.label, dataModel.finalised, importAsNewDocumentationVersion, targetConnectionName)
    }

    void copyFolderToTarget(UUID folderId, String targetConnectionName, UUID targetParentFolderId = null, boolean importAsNewDocumentationVersion,
                            String sourceConnectionName = defaultConnectionName) {
        Map sourceFolderMap = getFolderById(folderId, sourceConnectionName)
        UUID targetFolderId = createFolder(sourceFolderMap, targetParentFolderId, targetConnectionName)

        List<UUID> dataModelsInSourceFolder = listDataModelsInFolder(folderId, sourceConnectionName)
        dataModelsInSourceFolder.each {sourceDataModelId ->
            copyDataModelToTarget(sourceDataModelId, targetConnectionName, targetFolderId, importAsNewDocumentationVersion, sourceConnectionName)
        }

        List<UUID> subFoldersInSourceFolder = listSubFoldersInFolder(folderId, sourceConnectionName)
        subFoldersInSourceFolder.each {sourceSubFolderId ->
            copyFolderToTarget(sourceSubFolderId, targetConnectionName, targetFolderId, importAsNewDocumentationVersion, sourceConnectionName)
        }
    }
}
