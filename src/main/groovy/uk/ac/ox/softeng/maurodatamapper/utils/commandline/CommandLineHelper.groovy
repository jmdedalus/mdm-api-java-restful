/*
 * Copyright 2020-2024 University of Oxford and NHS England
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

    BasicCommandOptions options

    CommandLineHelper(BasicCommandOptions command) {
        super(command)
        options = command
    }

    @Override
    ParseResult parseArgs(String... args) {
        if(args.size() == 0) {
            this.usage(System.out)
            System.exit(this.getCommandSpec().exitCodeOnUsageHelp())
        }
        BasicCommandOptions basicCommandOptions = new BasicCommandOptions()
        CommandLine basicCommandLine = new CommandLine(basicCommandOptions)
        basicCommandLine.setUnmatchedArgumentsAllowed(true)
        basicCommandLine.setOverwrittenOptionsAllowed(true)
        basicCommandLine.getCommandSpec().parser().collectErrors(true)
        basicCommandLine.parseArgs(args)

        String[] updatedArgs
        if (basicCommandOptions.usageHelpRequested) {
            this.usage(System.out)
            System.exit(this.getCommandSpec().exitCodeOnUsageHelp())
        }
        if (basicCommandOptions.propertyFiles) {
            List<String> updatedArgList = []
            updatedArgList.addAll(args)
            basicCommandOptions.propertyFiles.each { propertyFile ->
                try {
                    InputStream input = new FileInputStream(propertyFile)
                    Properties properties = new Properties()
                    properties.load(input)
                    updatedArgList.addAll(properties.collect{ key, value ->
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
            updatedArgs = Arrays.copyOf(updatedArgList.toArray(), updatedArgList.size(), String[].class)
        } else {
            updatedArgs = args
        }

        this.setUnmatchedArgumentsAllowed(true)
        this.setOverwrittenOptionsAllowed(true)
        this.getCommandSpec().parser().collectErrors(true)
        println 'updatedArgs = ' + updatedArgs
        println 'CommandLine::parseArgs'
        super.parseArgs(updatedArgs)

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
