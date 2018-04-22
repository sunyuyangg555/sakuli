/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2017 the original author or authors.
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

package org.sakuli.loader;

import org.sakuli.datamodel.TestCase;
import org.sakuli.exceptions.SakuliException;
import org.sakuli.exceptions.SakuliExceptionHandler;

/**
 * Hookable Callback interface which will be used to trigger some behaviours during a sakuli Action.
 *
 * @author tschneck
 *         Date: 4/19/17
 */
public interface ActionLoaderCallback {


    /**
     * Will be called after the basic initializing of a new {@link TestCase}.
     */
    void initTestCase(TestCase testCase);

    /**
     * Will be called after a exception is handled in {@link SakuliExceptionHandler}.
     */
    void handleException(SakuliException transformedException);

    /**
     * Will be called from {@link BeanLoader#releaseContext()}.
     */
    void releaseContext();
}