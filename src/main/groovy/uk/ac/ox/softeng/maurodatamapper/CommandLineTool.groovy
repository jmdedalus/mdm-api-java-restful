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
