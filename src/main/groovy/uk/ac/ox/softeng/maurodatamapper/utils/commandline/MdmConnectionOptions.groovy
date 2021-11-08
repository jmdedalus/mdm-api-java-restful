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

import uk.ac.ox.softeng.maurodatamapper.api.restful.client.BindingMauroDataMapperClient

import groovy.util.logging.Slf4j
import picocli.CommandLine

@CommandLine.Command
@Slf4j
class MdmConnectionOptions extends BasicCommandOptions {


    @CommandLine.Option(
            names = [ "-U", "--clientBaseUrl", "--client.baseUrl" ],
            description = [ "The base URL of the catalogue api.",
                "For example: 'http://www.example.com/metadata-catalogue/",
                "Any trailing '/api' will be added" ],
            required = true
    )
    URL clientBaseUrl

    @CommandLine.Option(
            names = [ "-u", "--clientUsername", "--client.username" ],
            description = [ "The username for logging into the metadata catalogue instance."]
    )
    String clientUsername

    @CommandLine.Option(
            names = [ "-p", "--clientPassword", "--client.password" ],
            description = [ "The password for logging into the metadata catalogue instance."],
            interactive = true,
            arity = "0..1"
    )
    char[] clientPassword

    @CommandLine.Option(
        names = [ "-a", "--clientApiKey", "--client.apiKey" ],
        description = [ "The API Key for logging into the metadata catalogue instance."]
    )
    String clientApiKey



    BindingMauroDataMapperClient getMauroDataMapperClient() {
        if(clientUsername && clientPassword) {
            return new BindingMauroDataMapperClient(clientBaseUrl.toString(), clientUsername, new String(clientPassword))
        } else if (clientApiKey) {
            UUID clientApiKeyUUID = UUID.fromString(clientApiKey)
            return new BindingMauroDataMapperClient(clientBaseUrl.toString(), clientApiKeyUUID)
        } else {
            log.error("username / password or apiKey must be set")
            return null
        }
    }


}