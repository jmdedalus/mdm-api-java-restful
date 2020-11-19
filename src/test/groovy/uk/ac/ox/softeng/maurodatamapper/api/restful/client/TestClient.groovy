package uk.ac.ox.softeng.maurodatamapper.api.restful.client


import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel

import groovy.util.logging.Slf4j

@Slf4j
class TestClient {

    static void main(String[] args) throws Exception {
        BindingMauroDataMapperClient client
        try {
            client = new BindingMauroDataMapperClient('http://localhost:8080', 'admin@maurodatamapper.com', 'password')


            DataModel dm = new DataModel(label: 'test api DM')
            dm.addToDataClasses(label: 'dc')
            UUID folderId = client.findOrCreateFolderByName('Development Folder')
            client.importDataModel(dm, folderId, 'test', false, false)


            //            client.openConnection('readerConnection', 'http://localhost:8080', 'reader@mdm.com', 'password')
            //            client.openConnection('editorConnection', 'http://localhost:8080', 'ecitor@mdm.com', 'password')
            //            client.openConnection('nhsd', 'http://nhsd/mdm', 'reader@nhs.uk', 'password')
            //
            //
            //            MauroDataMapperConnection nhsdConnection = client.getConnection('nhsd')
            //            MauroDataMapperConnection connection = client.getConnection(MauroDataMapperClient.DEFAULT_CONNECTION_NAME)
            //
            //            nhsdConnection.currentCookie != connection.currentCookie
            //
            //            connection.GET('admin/status')
            //
            //            client.createFolder('new folder', null) // works as admin user
            //            client.createFolder('new folder', null, 'editorConnection') // works as editor user
            //            client.createFolder('new folder', null, 'readerConnection') // does not work
            //
            //            client.copyDataModelToTarget('asdifgwieytr3iyu2', MauroDataMapperClient.DEFAULT_CONNECTION_NAME, '832749823y9rg329',
            //            false, 'nhsd')


        } catch (Exception exception) {
            if (client) {
                client.close()
            }
            log.error('Failed', exception)
            System.exit(1)
        }
        if (client) {
            client.close()
        }
        System.exit(0)
    }
}
