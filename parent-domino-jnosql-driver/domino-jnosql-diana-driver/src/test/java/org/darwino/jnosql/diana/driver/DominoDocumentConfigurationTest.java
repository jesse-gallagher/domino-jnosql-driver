/**
 * Copyright © 2017 Jesse Gallagher
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
 * Includes code derived from the JNoSQL Diana Couchbase driver and Artemis
 * extensions, copyright Otavio Santana and others and available from:
 *
 * https://github.com/eclipse/jnosql-diana-driver/tree/master/couchbase-driver
 * https://github.com/eclipse/jnosql-artemis-extension/tree/master/couchbase-extension
 */
package org.darwino.jnosql.diana.driver;

import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;
import org.junit.Test;
import org.openntf.domino.jnosql.diana.document.DominoDocumentConfiguration;

import static org.junit.Assert.assertNotNull;


public class DominoDocumentConfigurationTest extends AbstractDominoAppTest  {

    @SuppressWarnings({ "rawtypes" })
	@Test
    public void shouldCreateDocumentCollectionManagerFactoryByMap() {

        DominoDocumentConfiguration configuration = new DominoDocumentConfiguration();
        DocumentCollectionManagerFactory managerFactory = configuration.get();
        assertNotNull(managerFactory);
    }

    @SuppressWarnings({ "rawtypes" })
	@Test
    public void shouldCreateDocumentCollectionManagerFactoryByFile() {
        DominoDocumentConfiguration configuration = new DominoDocumentConfiguration();
        DocumentCollectionManagerFactory managerFactory = configuration.get();
        assertNotNull(managerFactory);
    }
}