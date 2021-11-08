/*
 * Copyright 2020 University of Oxford
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.utils.commandline

import picocli.CommandLine

class CommandLineHelper extends CommandLine {

    CommandLineHelper(BasicCommandOptions command) {
        super(command)
    }

    @Override
    ParseResult parseArgs(String... args) {
        if(args.size() == 0) {
            this.usage(System.out)
            System.exit(this.getCommandSpec().exitCodeOnUsageHelp())
        }
        BasicCommandOptions basicCommandOptions = new BasicCommandOptions()
        //CatalogueCommandOptions fullOptions = new CatalogueConnectionOptions()

        CommandLine commandLine = new CommandLine(basicCommandOptions)
        commandLine.setUnmatchedArgumentsAllowed(true)
        commandLine.setOverwrittenOptionsAllowed(true)
        commandLine.getCommandSpec().parser().collectErrors(true)
        ParseResult parseResult = commandLine.parseArgs(args)

        if(basicCommandOptions.usageHelpRequested) {
            this.usage(System.out)
            System.exit(this.getCommandSpec().exitCodeOnUsageHelp())
        }
        if(basicCommandOptions.propertyFiles) {
            List<String> updatedArgs = []
            updatedArgs.addAll(args)
            basicCommandOptions.propertyFiles.each { propertyFile ->
                try {
                    InputStream input = new FileInputStream(propertyFile)
                    Properties properties = new Properties()
                    properties.load(input)
                    updatedArgs.addAll(properties.collect{ key, value ->
                        """--${key}=${value}""".toString()
                    })
                } catch (Exception e) {
                    System.err.println("""Cannot read properties from file: ${propertyFile.toString()}""")
                    System.err.println("""  ${e.getMessage()}""")
                    if (basicCommandOptions.verbose) {
                        e.printStackTrace()
                    } else {
                        System.err.println("  Use the [-v, --verbose] flag for a full stack trace")
                    }

                }
            }
            String[] stringArray = Arrays.copyOf(updatedArgs.toArray(), updatedArgs.size(), String[].class)

            //CommandLine fullCommandLine = new CommandLine(fullOptions)
            this.setUnmatchedArgumentsAllowed(true)
            this.setOverwrittenOptionsAllowed(true)
            this.getCommandSpec().parser().collectErrors(true)
            parseResult = super.parseArgs(stringArray)
        }

        List<Exception> exceptions = parseResult.errors()
        if (exceptions) {
            exceptions.each { exception ->
                if (exception instanceof MissingParameterException) {
                    System.err.println("Missing required parameter(s): ")
                    exception.missing.each { prop ->
                        System.err.println("""  ${prop.paramLabel()}""")
                        if (prop.description()) {
                            prop.description().each { description ->
                                System.err.println("""    $description""")
                            }
                        }
                    }
                } else if (exception instanceof ParameterException) {
                    System.err.println("Invalid parameter value for parameter: ")
                    System.err.println("""  ${exception.message}""")
                    if (options.verbose) {
                        exception.printStackTrace()
                    } else {
                        System.err.println("  Use the [-v, --verbose] flag for a full stack trace")
                    }
                }
                System.exit(0)
            }
        }
        return parseResult
    }

}
