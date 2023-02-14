package uk.ac.ox.softeng.maurodatamapper.api.restful.client

import spock.lang.Specification
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel

class RestfulClientSpec extends Specification {

    final static String URL = 'https://modelcatalogue.cs.ox.ac.uk/continuous-deployment/'
    final static UUID API_KEY = UUID.fromString('b13abe48-d85e-49a9-b6e5-d6506d653d40')

    BindingMauroDataMapperClient getClient() {

        BindingMauroDataMapperClient client = new BindingMauroDataMapperClient(URL, API_KEY)
        return client

    }



    void 'Test retrieving a data model' () {
        when: "Download and bind a data model"
        DataModel dm
        getClient().withCloseable {client ->
            dm = client.findAndExportAndBindDataModelByName("Complex Test DataModel")
        }
        then: "the data model is present and correct"
        dm != null
        dm.childDataClasses.size() == 3
        dm.dataClasses.size() == 4


    }


}
