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
package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint.CatalogueItemPrefix
import uk.ac.ox.softeng.maurodatamapper.core.authority.Authority
import uk.ac.ox.softeng.maurodatamapper.core.container.Classifier
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.facet.Annotation
import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.core.facet.ReferenceFile
import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLink
import uk.ac.ox.softeng.maurodatamapper.core.facet.VersionLink
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.SummaryMetadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.facet.summarymetadata.SummaryMetadataReport
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClassService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElementService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataTypeService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ModelDataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.enumeration.EnumerationValue
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.DataModelJsonImporterService
import uk.ac.ox.softeng.maurodatamapper.terminology.CodeSet
import uk.ac.ox.softeng.maurodatamapper.terminology.Terminology
import uk.ac.ox.softeng.maurodatamapper.terminology.item.Term
import uk.ac.ox.softeng.maurodatamapper.terminology.item.TermRelationshipType
import uk.ac.ox.softeng.maurodatamapper.terminology.item.term.TermRelationship
import uk.ac.ox.softeng.maurodatamapper.util.Utils

import grails.testing.gorm.DataTest
import grails.web.databinding.DataBinder
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.datastore.mapping.simple.SimpleMapDatastore
import org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor

@CompileStatic
@SuppressWarnings('unused')
class BindingMauroDataMapperClient extends MauroDataMapperClient implements DataBinder, DataTest {

    DataModelJsonImporterService dataModelJsonImporterService

    BindingMauroDataMapperClient(Properties properties) {
        super(properties)
        initialise()
    }

    BindingMauroDataMapperClient(String connectionName, Properties properties) {
        super(connectionName, properties)
        initialise()
    }

    BindingMauroDataMapperClient(String baseUrl, String username, String password) {
        super(baseUrl, username, password)
        initialise()
    }

    BindingMauroDataMapperClient(String baseUrl, UUID apiKey) {
        super(baseUrl, apiKey)
        initialise()
    }

    BindingMauroDataMapperClient(String connectionName, String baseUrl, String username, String password) {
        super(connectionName, baseUrl, username, password)
        initialise()
    }

    // Local only client
    BindingMauroDataMapperClient(String connectionName = DEFAULT_CONNECTION_NAME) {
        super(connectionName)
        initialise()
    }



    void initialise() {
        dataModelJsonImporterService = new DataModelJsonImporterService()
        dataModelJsonImporterService.dataModelService = new DataModelService()
        dataModelJsonImporterService.dataModelService.dataClassService = new DataClassService()
        dataModelJsonImporterService.dataModelService.dataClassService.dataElementService = new DataElementService()
        dataModelJsonImporterService.dataModelService.dataTypeService = new DataTypeService()

        new DataTestSetupSpecInterceptor().configureDataTest(this)
        SimpleMapDatastore simpleDatastore = this.applicationContext.getBean(SimpleMapDatastore)
        this.currentSession = simpleDatastore.connect()
        DatastoreUtils.bindSession(this.currentSession)
        mockDomains(DataModel, DataClass, DataElement, DataType, PrimitiveType, ReferenceType, EnumerationType, ModelDataType, EnumerationValue,
                    SummaryMetadata, SummaryMetadataReport,
                    Annotation, Metadata, ReferenceFile, SemanticLink, VersionLink, Classifier, Authority, Folder,
                    Term, Terminology, TermRelationship, TermRelationshipType, CodeSet)
    }

    void close() {
        super.close()

        if (this.currentSession != null) {
            this.currentSession.disconnect()
            DatastoreUtils.unbindSession(this.currentSession)
        }
        SimpleMapDatastore simpleDatastore = this.applicationContext.getBean(SimpleMapDatastore)
        simpleDatastore.clearData()

        cleanupGrailsApplication()
    }

    Folder getAndBindFolderById(UUID folderId, String connectionName = defaultConnectionName) {
        Map folderMap = getFolderById(folderId, connectionName)
        Folder folder = new Folder()
        bindData folder, folderMap
        folder
    }

    DataModel exportAndBindDataModelById(UUID id, String connectionName = defaultConnectionName) {
        Map exportModel = exportDataModel(id, connectionName)
        dataModelJsonImporterService.bindMapToDataModel(getConnection(connectionName).clientUser, exportModel)
    }

    DataModel findAndExportAndBindDataModelByName(String name, String connectionName = defaultConnectionName) {
        UUID dataModelId = findDataModelIdByName(name, connectionName)
        if (!dataModelId) return null
        exportAndBindDataModelById(dataModelId, connectionName)
    }

    List<SummaryMetadata> listAndBindSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                                     String connectionName = defaultConnectionName) {
        listSummaryMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect { summary ->
            SummaryMetadata summaryMetadata = new SummaryMetadata()
            bindData summaryMetadata, summary
            summaryMetadata.id = Utils.toUuid(summary.id as String)
        } as List<SummaryMetadata>
    }

    List<Metadata> listAndBindMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                       String connectionName = defaultConnectionName) {
        listMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect { md ->
            Metadata metadata = new Metadata()
            bindData metadata, md
            metadata.id = Utils.toUuid(md.id as String)
        } as List<Metadata>
    }

    void copyDataModelToTarget(UUID dataModelId, String targetConnectionName, UUID targetFolderId, boolean importAsNewBranchModelVersion,
                               boolean importAsNewDocumentationVersion,
                               String sourceConnectionName = defaultConnectionName) {
        DataModel dataModel = exportAndBindDataModelById(dataModelId, sourceConnectionName)
        importDataModel(dataModel, targetFolderId, dataModel.label, dataModel.finalised, importAsNewBranchModelVersion, importAsNewDocumentationVersion, targetConnectionName)

    }

    void copyFolderToTarget(UUID folderId, String targetConnectionName, UUID targetParentFolderId = null,boolean importAsNewBranchModelVersion,  boolean importAsNewDocumentationVersion,
                            String sourceConnectionName = defaultConnectionName) {
        Map sourceFolderMap = getFolderById(folderId, sourceConnectionName)
        UUID targetFolderId = createFolder(sourceFolderMap, targetParentFolderId, targetConnectionName)

        List<UUID> dataModelsInSourceFolder = listDataModelsInFolder(folderId, sourceConnectionName)
        dataModelsInSourceFolder.each {sourceDataModelId ->
            copyDataModelToTarget(sourceDataModelId, targetConnectionName, targetFolderId, importAsNewBranchModelVersion, importAsNewDocumentationVersion, sourceConnectionName)
        }

        List<UUID> subFoldersInSourceFolder = listSubFoldersInFolder(folderId, sourceConnectionName)
        subFoldersInSourceFolder.each {sourceSubFolderId ->
            copyFolderToTarget(sourceSubFolderId, targetConnectionName, targetFolderId, importAsNewBranchModelVersion, importAsNewDocumentationVersion, sourceConnectionName)
        }
    }
}
