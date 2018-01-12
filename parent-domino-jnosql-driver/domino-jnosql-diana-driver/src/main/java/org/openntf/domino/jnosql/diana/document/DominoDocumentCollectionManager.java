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
package org.openntf.domino.jnosql.diana.document;

import java.util.List;
import java.util.Map;

import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentEntity;

/**
 * The Darwino implementation of {@link DocumentCollectionManager}
 */
public interface DominoDocumentCollectionManager extends DocumentCollectionManager {
	/**
     * Executes the query with params and then returns the result
     *
     * @param query the query
     * @param params    the params
     * @return the query result
     */
    List<DocumentEntity> query(String query, Map<String, Object> params);
    
	/**
     * Executes the query and then returns the result
     *
     * @param query the query
     * @return the query result
     */
    List<DocumentEntity> query(String query);
    
    /**
     * Searches in Domino using Full Text Search
     *
     * @param query the query to be used
     * @return the elements from the query
     */
    List<DocumentEntity> search(String query);
}