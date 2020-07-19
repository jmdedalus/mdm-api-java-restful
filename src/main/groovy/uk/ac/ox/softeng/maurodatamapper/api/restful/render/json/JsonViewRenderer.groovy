package uk.ac.ox.softeng.maurodatamapper.api.restful.render.json


import grails.plugin.json.view.JsonViewConfiguration
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.api.JsonView
import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdRenderer
import grails.plugin.json.view.api.jsonapi.JsonApiIdRenderStrategy
import groovy.text.Template
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.keyvalue.mapping.config.KeyValueMappingContext
import org.grails.datastore.mapping.model.MappingContext
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource

@Singleton
@CompileStatic
class JsonViewRenderer {

    MessageSource messageSource = new StaticMessageSource()

    MappingContext mappingContext = {
        def ctx = new KeyValueMappingContext("restful_client")
        ctx.setCanInitializeEntities(true)
        return ctx
    }()

    JsonViewConfiguration viewConfiguration = new JsonViewConfiguration()

    JsonApiIdRenderStrategy jsonApiIdRenderStrategy = new DefaultJsonApiIdRenderer()

    JsonViewTemplateEngine templateEngine

    void initialise() {
        templateEngine = new JsonViewTemplateEngine(viewConfiguration, Thread.currentThread().contextClassLoader)
        if (messageSource != null) {
            templateEngine.setMessageSource(messageSource)
        }
        if (mappingContext != null) {
            templateEngine.setMappingContext(mappingContext)
        }
        templateEngine.setJsonApiIdRenderStrategy(jsonApiIdRenderStrategy)
    }

    /**
     * Render a domain object using the appropriate template.
     * If templateSource is supplied then this will be used rather than attempting to find the correct template or view
     *
     *
     * @param domain The domain object to render
     * @param templateSource The raw source of the template
     * @return
     */
    String renderDomain(domain, String templateSource = null) {
        if (!domain) return renderEmpty()
        if (templateSource) render(templateSource, getRenderModel(domain))
        else render(template: getDomainTemplateUri(domain), model: getRenderModel(domain))
    }

    /**
     * Render one of the GSON views in the grails-app/views directory for the given arguments using the given domain.
     *
     * @param domain The domain object to render
     * @param arguments The named arguments: 'template' or 'view'
     * @return
     */
    String renderDomain(domain, Map arguments) {
        arguments.model = getRenderModel(domain)
        render(arguments)
    }

    /**
     * Render a template for the given source
     *
     * @param templateSource The raw source of the template
     * @param model The model
     *
     * @return The result
     */
    String render(String templateSource, Map model) {
        def template = templateEngine.createTemplate(templateSource)
        writeBody(template, model)
    }

    /**
     * Render one of the GSON views in the grails-app/views directory for the given arguments
     *
     * @param arguments The named arguments: 'template', 'view' and 'model'
     *
     * @return The render result
     */
    String render(Map arguments) {

        String viewUri
        if (arguments.template) {
            viewUri = templateEngine
                .viewUriResolver
                .resolveTemplateUri(null, arguments.template.toString())

        } else if (arguments.view) {
            viewUri = arguments.view.toString()
        } else {
            throw new IllegalArgumentException("Either a 'view' or 'template' argument is required!")

        }
        def template = templateEngine.resolveTemplate(viewUri)

        if (template == null) {
            throw new IllegalArgumentException("No view or template found for URI $viewUri")
        }

        def model = arguments.model instanceof Map ? (Map) arguments.model : [:]
        return writeBody(template, model)
    }

    static String renderEmpty() {
        ''
    }

    static String getDomainTemplateUri(domain) {
        String domainName = domain.class.simpleName
        "/${domainName.uncapitalize()}/${domainName.uncapitalize()}"
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    static Map getRenderModel(domain) {
        Map<String, Object> map = [pageView: true,] as Map<String, Object>
        map.put("${domain.class.simpleName.uncapitalize()}".toString(), domain)
        map
    }

    private static String writeBody(Template template, Map model) {
        JsonView writable = (JsonView) template.make(model)
        def sw = new StringWriter()
        writable.writeTo(sw)
        sw.toString()
    }

}
