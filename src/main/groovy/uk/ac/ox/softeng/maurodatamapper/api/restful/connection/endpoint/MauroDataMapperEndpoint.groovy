package uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint

import io.micronaut.http.uri.UriBuilder

enum MauroDataMapperEndpoint {

    LOGIN('authentication/login'),
    AUTHENTICATED_SESSION('/session/isAuthenticated'),
    LOGOUT('authentication/logout'),

    DATAMODEL_IMPORTERS('dataModels/providers/importers'),
    DATAMODEL_EXPORTERS('dataModels/providers/exporters'),

    TERMINOLOGY_IMPORTERS('terminologies/providers/importers'),
    TERMINOLOGY_EXPORTERS('terminologies/providers/exporters'),

    DATAMODEL_IMPORT('dataModels/import/${importerNamespace}/${importerName}/${importerVersion}'),
    TERMINOLOGY_IMPORT('terminologies/import/${importerNamespace}/${importerName}/${importerVersion}'),

    SUMMARY_METADATA_LIST('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata'),
    SUMMARY_METADATA_CREATE('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata'),
    SUMMARY_METADATA_ID('{catalogueItemDomainType}/{catalogueItemId}/summaryMetadata/{id}'),

    METADATA_LIST('{catalogueItemDomainType}/{catalogueItemId}/metadata'),
    METADATA_CREATE('{catalogueItemDomainType}/{catalogueItemId}/metadata'),
    METADATA_ID('{catalogueItemDomainType}/{catalogueItemId}/metadata/{id}'),

    FOLDER_CREATE('folders'),
    FOLDERS_LIST('folders/?offset=0&max=1000'),

    FOLDER_ID('folders/{id}'),
    FOLDER_DELETE("folders/{id}?permanent={permanent}"),
    FOLDER_FOLDER_CREATE("folders/{folderId}/folders"),

    FOLDER_LIST_FOLDERS("folders/{folderId}/folders?offset=0&max=1000"),
    FOLDER_LIST_DATAMODELS("folders/{folderId}/dataModels"),
    FOLDER_LIST_TERMINOLOGIES("folders/{folderId}/terminologies"),
    FOLDER_LIST_CODESETS("folders/{folderId}/codeSets"),

    DATAMODEL_LIST('dataModels'),
    DATAMODEL_ID('dataModels/{id}'),
    DATAMODEL_DELETE("dataModels/{dataModelId}?permanent={permanent}"),
    DATAMODEL_EXPORT("dataModels/{dataModelId}/export/{exporterNamespace}/{exporterName}/{exporterVersion}"),

    DATACLASS_ID("dataModels/{dataModelId}/dataClasses/{id}"),
    DATACLASS_DATACLASS_ID("dataModels/{dataModelId}/dataClasses/{dataClassId}/dataClasses/{id}"),
    DATAELEMENT_ID("dataModels/{dataModelId}/dataClasses/{dataClassId}/dataElements/{id}"),

    TERMINOLOGY_LIST('terminologies'),
    TERMINOLOGY_ID('terminologies/{id}'),
    TERMINOLOGY_DELETE("terminologies/{terminologyId}?permanent={permanent}"),
    TERMINOLOGY_EXPORT("terminologies/{terminologyId}/export/{exporterNamespace}/{exporterName}/{exporterVersion}"),

    TERM_ID("terminologies/{terminologyId}/terms/{termId}"),

    CODESET_ID('codeSets/{id}'),
    CODESET_DELETE("codeSets/{id}?permanent={permanent}")

    String representation

    MauroDataMapperEndpoint(String representation) {
        this.representation = representation
    }

    String build(Map<String, ? super Object> params = [:]) {
        UriBuilder.of(representation)
            .expand(params)
            .toString()
    }
}