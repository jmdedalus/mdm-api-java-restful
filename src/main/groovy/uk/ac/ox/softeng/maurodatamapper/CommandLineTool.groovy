package uk.ac.ox.softeng.maurodatamapper

import uk.ac.ox.softeng.maurodatamapper.utils.commandline.MdmCommandLineTool
import uk.ac.ox.softeng.maurodatamapper.utils.commandline.MdmConnectionOptions
import groovy.util.logging.Slf4j

@Slf4j
class CommandLineTool extends MdmCommandLineTool<MdmConnectionOptions> {
    static void main(String[] args) {
        CommandLineTool commandLineTool = new CommandLineTool(args)
        commandLineTool.run()
    }

    CommandLineTool(String[] args) {
        super(args, MdmConnectionOptions)
    }

    void run() {
        log.debug('CommandLineTool::run')
        log.debug('options.clientBaseUrl = {}', options.clientBaseUrl)
    }
}
