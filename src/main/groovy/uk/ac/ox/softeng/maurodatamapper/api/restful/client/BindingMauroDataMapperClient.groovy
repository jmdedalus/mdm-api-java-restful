package uk.ac.ox.softeng.maurodatamapper.api.restful.client

class BindingMauroDataMapperClient extends MauroDataMapperClient {

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

    /*
        List<SummaryMetadata> listAndBindSummaryMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                                         String connectionName = defaultConnectionName) {
            listSummaryMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect {summary ->
                SummaryMetadata summaryMetadata = new SummaryMetadata()
                bindData summaryMetadata, summary
                summaryMetadata.id = Utils.toUuid(summary.id)
            } as List<SummaryMetadata>
        }

        List<Metadata> listAndBindMetadata(CatalogueItemPrefix catalogueItemPrefix, UUID catalogueItemId,
                                           String connectionName = defaultConnectionName) {
            listMetadata(catalogueItemPrefix, catalogueItemId, connectionName).collect {md ->
                Metadata metadata = new Metadata()
                bindData metadata, md
                metadata.id = Utils.toUuid(md.id)
            } as List<Metadata>
        }

        DataModel getDataModelById(UUID id, String connectionName = defaultConnectionName) {
            Map exporterProperties
            def restResponse = exportDataModel("ox.softeng.metadatacatalogue.core.spi.json", "JsonExporterService", "1.1", id, connectionName)
            DataModel returnDataModel
            returnDataModel =
                application.jsonImporterService.bindMapToDataModel getConnection(connectionName).loggedInUser, new HashMap(restResponse.json
                .dataModel)
            return returnDataModel
        }

        DataModel getDataModelByName(String name, String connectionName = defaultConnectionName) {
            UUID dataModelId = findDataModelByName(name, connectionName)
            if (dataModelId == null) {
                return null
            }
            def restResponse = exportDataModel("ox.softeng.metadatacatalogue.core.spi.json", "JsonExporterService", "1.1", dataModelId,
            connectionName)
            DataModel returnDataModel
            returnDataModel =
                application.jsonImporterService.bindMapToDataModel getConnection(connectionName).loggedInUser, new HashMap(restResponse.json
                .dataModel)
            return returnDataModel
        }


        Folder getFolderById(UUID folderId, String connectionName = defaultConnectionName) {

            getConnection(connectionName).GET(MauroDataMapperEndpoint.FOLDER_ID, [folderId.toString()])

            Folder folder = new Folder()
            bindData folder, restResponse.json

            return folder
        }


        DataModel bindDataModelFromMap(Map dataModelMap, CatalogueUser catalogueUser) {
            DataModel returnDataModel =
                application.jsonImporterService.bindMapToDataModel catalogueUser, dataModelMap
            return returnDataModel
        }
           private SummaryMetadata newSummaryMetadata(String name, String description, SummaryMetadataType summaryMetadataType,
                                                   List<SummaryMetadataReport> summaryMetadataReports) {
            SummaryMetadata summaryMetadata = new SummaryMetadata(name: name, description: description, summaryMetadataType: summaryMetadataType)
            summaryMetadataReports.each {summaryMetadata.addToSummaryMetadataReports(it)}

            summaryMetadata
        }

        private SummaryMetadataReport newSummaryMetadataReport(Map reportValue) {
            new SummaryMetadataReport(
                reportDate: OffsetDateTime.now(),
                reportValue: JsonOutput.toJson(reportValue)
            )
        }
    */
}
