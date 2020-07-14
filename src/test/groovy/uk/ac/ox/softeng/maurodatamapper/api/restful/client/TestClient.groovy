package uk.ac.ox.softeng.maurodatamapper.api.restful.client

class TestClient {

    static void main(String[] args) {

        Properties sourceProperties = new Properties()
        sourceProperties.load(MauroDataMapperClient.getClassLoader().getResourceAsStream("config.properties"))

        Properties targetProperties = new Properties()
        targetProperties.load(MauroDataMapperClient.getClassLoader().getResourceAsStream("config.properties"))

        MauroDataMapperClient client = new MauroDataMapperClient("source", sourceProperties)
        client.openConnection("target", targetProperties)

    }
}
