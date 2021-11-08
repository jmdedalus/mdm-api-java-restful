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

import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command
class BasicCommandOptions {

    @Option(names = ["-h", "--help"], usageHelp = true, description = "display this help message")
    boolean usageHelpRequested

    @Option(names = ["-v", "--verbose"], description = "Verbose")
    boolean verbose


    @Option(names=["-P", "--properties"],
            description = ["Additional properties file",
                            "All command-line properties can be provided via properties files",
                            "--properties ./config.properties"])
    List<File> propertyFiles

    @Option(names=["-D", "--debug"],
            description = ["Debug - provide helpful information back to the command-line"])
    boolean debug

}
