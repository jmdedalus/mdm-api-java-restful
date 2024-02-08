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


import java.lang.reflect.Field

abstract class MdmCommandLineTool<T extends BasicCommandOptions> {

    T options

    MdmCommandLineTool(String[] args, Class<T> clazz) {
        options = clazz.getDeclaredConstructor().newInstance()
        CommandLineHelper commandLineHelper = new CommandLineHelper(options)
        println 'args = ' + args
        println 'options = ' + options
        println 'options.clientBaseUrl = ' + options.clientBaseUrl
        commandLineHelper.parseArgs(args)
        if(options.debug) {
            System.out.println("Command Line options: ${options.getClass().getName()}")
            getAllFields(options.getClass()).findAll{!it.synthetic}.each {
                System.out.println("  ${it.name} : ${options."$it.name"}")
            }
        }
    }

    MdmCommandLineTool(T options) {
        this.options = options
    }

    MdmCommandLineTool() {
        // Default constructor (shouldn't be used)
    }

    private List<Field> getAllFields(Class clazz) {
        if (clazz == null) {
            return Collections.emptyList()
        }

        List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()))
        List<Field> filteredFields = clazz.getDeclaredFields()
                .findAll{ !it.synthetic}
        result.addAll(filteredFields)
        return result
    }


}
