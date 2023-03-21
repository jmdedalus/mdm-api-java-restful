/*
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.utils.commandline;

import picocli.CommandLine;

@CommandLine.Command
class MdmImporterOptions extends MdmConnectionOptions {

    @CommandLine.Option(
            names = [ "-t", "--testOnly", "--csv.testOnly" ],
            description = [ "If 'testOnly' is set, then the program will simply output the discovered columns and types, and not upload a model to the catalogue."],
            required = false
    )
    boolean testOnly = true


    @CommandLine.Option(
            names = [ "-f", "--folderPath", "--import.folderPath" ],
            description = [ "A path specifying the folder that models should be imported into.",
            "Folders should be separated with a period ('.') character "],
            required = false
    )
    String folderPath

}
