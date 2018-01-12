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

import org.jnosql.diana.api.document.DocumentCollectionManagerAsyncFactory;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;
import org.openntf.domino.Database;
import org.openntf.domino.Session;

import java.util.Objects;

public class DominoDocumentCollectionManagerFactory implements DocumentCollectionManagerFactory<DominoDocumentCollectionManager>,
		DocumentCollectionManagerAsyncFactory<DominoDocumentCollectionManagerAsync> {
	
	private Session session;
	
	DominoDocumentCollectionManagerFactory(Session session) {
		this.session = session;
	}

	@Override
	public DominoDocumentCollectionManagerAsync getAsync(String database) throws UnsupportedOperationException, NullPointerException {
		return new DefaultDominoDocumentCollectionManagerAsync(get(database));
	}

	@Override
	public DominoDocumentCollectionManager get(String database) throws UnsupportedOperationException, NullPointerException {
		Objects.requireNonNull(database, "database is required");

		Database db = session.getDatabase(database);
		return new DefaultDominoDocumentCollectionManager(db);
	}

	@Override
	public void close() {
		this.session = null;
	}
}