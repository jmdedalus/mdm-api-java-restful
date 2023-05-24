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
package uk.ac.ox.softeng.maurodatamapper.utils.commandline

import uk.ac.ox.softeng.maurodatamapper.api.restful.client.BindingMauroDataMapperClient

import groovy.util.logging.Slf4j
import picocli.CommandLine

@CommandLine.Command
@Slf4j
class MdmConnectionOptions extends BasicCommandOptions {


    @CommandLine.Option(
            names = [ "-U", "--url", "--client.baseUrl" ],
            description = [ "The base URL of the Mauro API.",
                "For example: 'http://www.example.com/metadata-catalogue/",
                "Any trailing '/api' will be added" ],
            required = true
    )
    URL clientBaseUrl

    @CommandLine.Option(
            names = [ "-u", "--username", "--client.username" ],
            description = [ "The username for logging into the Mauro instance."]
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
            names = [ "-a", "--api-key", "--client.apiKey" ],
            description = [ "The API Key for logging into the Mauro instance."]
    )
    String clientApiKey

    @CommandLine.Option(
            names = [ "-i", "--insecure", "--client.insecure" ],
            description = [ "Allow insecure TLS connections (eg, self-signed certificates or mismatched hostnames)"],
            defaultValue = "false"
    )
    Boolean clientInsecureTls


    BindingMauroDataMapperClient getBindingMauroDataMapperClient() {
        if(clientUsername && clientPassword) {
            return new BindingMauroDataMapperClient(clientBaseUrl.toString(), clientUsername, new String(clientPassword), clientInsecureTls)
        } else if (clientApiKey) {
            UUID clientApiKeyUUID = UUID.fromString(clientApiKey)
            return new BindingMauroDataMapperClient(clientBaseUrl.toString(), clientApiKeyUUID, clientInsecureTls)
        } else {
            log.error("username / password or apiKey must be set")
            return null
        }
    }
}