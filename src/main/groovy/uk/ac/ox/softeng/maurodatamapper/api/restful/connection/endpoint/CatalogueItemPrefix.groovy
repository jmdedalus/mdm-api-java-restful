package uk.ac.ox.softeng.maurodatamapper.api.restful.connection.endpoint

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
