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

import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentCollectionManagerAsync;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.document.Documents;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.openntf.domino.jnosql.diana.document.DominoDocumentCollectionManagerFactory;
import org.openntf.domino.jnosql.diana.document.DominoDocumentConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jnosql.diana.api.document.query.DocumentQueryBuilder.delete;
import static org.jnosql.diana.api.document.query.DocumentQueryBuilder.select;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("nls")
public class DominoDocumentCollectionManagerAsyncTest extends AbstractDominoAppTest {

	public static final String COLLECTION_NAME = "person";

	private DocumentCollectionManagerAsync entityManagerAsync;

	private DocumentCollectionManager entityManager;

	@AfterClass
	public static void afterClass() throws InterruptedException {
		Thread.sleep(1_000L);
	}

	@Before
	public void setUp() {
		DominoDocumentConfiguration configuration = new DominoDocumentConfiguration();
		DominoDocumentCollectionManagerFactory managerFactory = configuration.get();
		entityManagerAsync = managerFactory.getAsync(BLANK_DB);
		entityManager = managerFactory.get(BLANK_DB);
		DocumentEntity documentEntity = getEntity();
		Optional<Document> id = documentEntity.find("name");
		@SuppressWarnings("unused")
		DocumentQuery query = select().from(COLLECTION_NAME)
				.where("name").eq(id.get().get())
				.build();
		DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME)
				.where("name").eq(id.get().get())
				.build();
		entityManagerAsync.delete(deleteQuery);
	}

	@Test
	public void shouldSaveAsync() throws InterruptedException {
		DocumentEntity entity = getEntity();
		entityManagerAsync.insert(entity);

		Thread.sleep(1_000L);
		Optional<Document> id = entity.find("name");
		DocumentQuery query = select().from(COLLECTION_NAME)
				.where("name").eq(id.get().get())
				.build();
		List<DocumentEntity> entities = entityManager.select(query);
		assertFalse(entities.isEmpty());

	}

	@Test
    public void shouldUpdateAsync() throws Exception {
	    	try {
	        DocumentEntity entity = getEntity();
	        @SuppressWarnings("unused")
			DocumentEntity documentEntity = entityManager.insert(entity);
	        Document newField = Documents.of("newField", "10");
	        entity.add(newField);
	        entityManagerAsync.update(entity);
	    	} catch(Exception e) {
	    		e.printStackTrace();
	    		throw e;
	    	}
    }

	@Test
	public void shouldRemoveEntityAsync() throws InterruptedException {
		DocumentEntity documentEntity = entityManager.insert(getEntity());
		Optional<Document> id = documentEntity.find("name");
		@SuppressWarnings("unused")
		DocumentQuery query = select().from(COLLECTION_NAME)
				.where("name").eq(id.get().get())
				.build();
		DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_NAME)
				.where("name").eq(id.get().get())
				.build();
		entityManagerAsync.delete(deleteQuery);
	}

	private DocumentEntity getEntity() {
		DocumentEntity entity = DocumentEntity.of(COLLECTION_NAME);
		Map<String, Object> map = new HashMap<>();
		map.put("name", "Poliana");
		map.put("city", "Salvador");
		map.put("_id", "id" + System.currentTimeMillis());
		List<Document> documents = Documents.of(map);
		documents.forEach(entity::add);
		return entity;
	}
}