/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2016 the original author or authors.
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
 */

package org.sakuli.services.forwarder.checkmk;

import org.sakuli.services.forwarder.AbstractTemplateOutputBuilder;
import org.sakuli.services.forwarder.configuration.TemplateModelEntityName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Georgi Todorov
 */
@ProfileCheckMK
@Component
public class CheckMKTemplateOutputBuilder extends AbstractTemplateOutputBuilder {

    @Autowired
    private CheckMKProperties checkMKProperties;

    @Override
    public String getConverterName() {
        return "Check_MK";
    }

    @Override
    public Map<TemplateModelEntityName, Object> getSpecificModelEntities() {
        return Collections.unmodifiableMap(new HashMap<TemplateModelEntityName, Object>() {
            {
                put(TemplateModelEntityName.CHECK_MK_PROPERTIES, checkMKProperties);
            }
        });
    }

}
