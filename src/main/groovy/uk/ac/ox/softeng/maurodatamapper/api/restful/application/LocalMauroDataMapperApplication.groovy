package uk.ac.ox.softeng.maurodatamapper.api.restful.application

import grails.boot.GrailsApp
import grails.plugin.json.view.JsonViewTemplateEngine
import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationContext

@Slf4j
@Singleton
class LocalMauroDataMapperApplication implements Closeable {

    //    JsonImporterService jsonImporterService
    //    JsonExporterService jsonExporterService
    //    XmlTerminologyExporterService xmlTerminologyExporterService
    //    ImporterService importerService
    //    MetadataCataloguePluginService metadataCataloguePluginService

    protected JsonViewTemplateEngine templateEngine

    private static ApplicationContext applicationContext

    private Map<String, String> gormProperties = [
        'grails.bootstrap.skip'     : 'true',
        'grails.env'                : 'custom',
        'server.port'               : '9000',
        'spring.flyway.enabled'     : 'false',
        'dataSource.driverClassName': 'org.h2.Driver',
        'dataSource.dialect'        : 'org.hibernate.dialect.H2Dialect',
        'dataSource.username'       : 'sa',
        'dataSource.password'       : '',
        'dataSource.dbCreate'       : 'create-drop',
        'dataSource.url'            : 'jdbc:h2:mem:remoteDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=TRUE',
    ]

    void start() {
        log.info('Starting Grails Application to handle GORM')
        gormProperties.each {k, v -> System.setProperty(k, v as String)}

        //        applicationContext = GrailsApp.run(Application)
        //
        //        HibernateDatastore hibernateDatastore = applicationContext.getBean(HibernateDatastore)
        //
        //        TransactionSynchronizationManager.bindResource(hibernateDatastore.getSessionFactory(),
        //                                                       new SessionHolder(hibernateDatastore.openSession()))
        //
        //        jsonImporterService = applicationContext.getBean(JsonImporterService)
        //        jsonExporterService = applicationContext.getBean(JsonExporterService)
        //        metadataCataloguePluginService = applicationContext.getBean(MetadataCataloguePluginService)
        //        importerService = applicationContext.getBean(ImporterService)
        //        xmlTerminologyExporterService = applicationContext.getBean(XmlTerminologyExporterService)

        templateEngine = applicationContext.getBean(JsonViewTemplateEngine)
    }

    void close() throws IOException {
        log.debug('Shutting down Grails Application')
        if (applicationContext != null) GrailsApp.exit(applicationContext)
    }

    protected String writeBody(Object obj, String templateUri = null) {
        String actualTemplateUri = templateUri
        if (!actualTemplateUri) {
            actualTemplateUri = getDomainTemplateUri(obj)
        }

        def template = templateEngine.resolveTemplate(actualTemplateUri)
        def writable = template.make(getRenderModel(obj))

        def sw = new StringWriter()
        writable.writeTo(sw)

        return sw.toString()
    }

    protected String getDomainTemplateUri(domain) {
        String domainName = domain.class.simpleName
        "/${domainName.uncapitalize()}/_${domainName.uncapitalize()}.gson"
    }

    protected Map getRenderModel(domain) {
        Map<String, Object> map = [:]
        map["${domain.class.simpleName.uncapitalize()}".toString()] = domain
        map
    }


}
