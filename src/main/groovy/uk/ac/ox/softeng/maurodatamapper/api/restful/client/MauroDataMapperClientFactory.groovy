package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import groovy.transform.CompileStatic

import java.nio.file.Paths

@CompileStatic
class MauroDataMapperClientFactory {

    static MauroDataMapperClient newFromPropertiesPath(String path) {
        InputStream propertiesFileInputStream = new FileInputStream(path)
        Properties clientProperties = new Properties()
        clientProperties.load(propertiesFileInputStream)
        new MauroDataMapperClient(clientProperties)
    }

    static MauroDataMapperClient newFromPropertiesUrl(URL propertiesFileUrl) {
        File propertiesFile = Paths.get(propertiesFileUrl.toURI()).toFile()
        InputStream propertiesFileInputStream = new FileInputStream(propertiesFile)
        Properties clientProperties = new Properties()
        clientProperties.load(propertiesFileInputStream)
        new MauroDataMapperClient(clientProperties)
    }
}
