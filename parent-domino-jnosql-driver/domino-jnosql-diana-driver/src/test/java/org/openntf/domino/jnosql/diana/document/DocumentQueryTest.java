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

import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openntf.domino.jnosql.diana.document.DominoDocumentCollectionManager;
import org.openntf.domino.jnosql.diana.document.DominoDocumentCollectionManagerFactory;
import org.openntf.domino.jnosql.diana.document.DominoDocumentConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.jnosql.diana.api.document.query.DocumentQueryBuilder.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DocumentQueryTest extends AbstractDominoAppTest  {

	public static final String COLLECTION_NAME = "person";
    static String id1 = "id1" + System.nanoTime();
    static String id2 = "id2" + System.nanoTime();
    static String id3 = "id3" + System.nanoTime();
    static String id4 = "id4" + System.nanoTime();
    private DominoDocumentCollectionManager entityManager;

    {
        DominoDocumentConfiguration configuration = new DominoDocumentConfiguration();
        DominoDocumentCollectionManagerFactory managerFactory = configuration.get();
        entityManager = managerFactory.get(BLANK_DB);
    }

    @AfterClass
    public static void afterClass() {
    }
    

	@BeforeClass
    public static void beforeClass() throws InterruptedException {
        DominoDocumentConfiguration configuration = new DominoDocumentConfiguration();
        DominoDocumentCollectionManagerFactory managerFactory = configuration.get();
        DominoDocumentCollectionManager entityManager = managerFactory.get(BLANK_DB);

        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));
        DocumentEntity entity2 = DocumentEntity.of("person", asList(Document.of("_id", id2)
                , Document.of("name", "name")));
        DocumentEntity entity3 = DocumentEntity.of("person", asList(Document.of("_id", id3)
                , Document.of("name", "name")));
        DocumentEntity entity4 = DocumentEntity.of("person", asList(Document.of("_id", id4)
                , Document.of("name", "name3")));

        entityManager.insert(Arrays.asList(entity, entity2, entity3, entity4));
        Thread.sleep(2_000L);

    }


    @Test
    public void shouldShouldDefineLimit() {

        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));

        Optional<Document> name = entity.find("name");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("name").eq(name.get().get())
                .limit(2L)
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertEquals(2, entities.size());

    }

    @Test
    public void shouldShouldDefineStart()  {
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));

        Optional<Document> name = entity.find("name");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("name").eq(name.get().get())
                .start(1L)
                .build();
        List<DocumentEntity> entities = entityManager.select(query);
        assertEquals(2, entities.size());

    }

    @Test
    public void shouldShouldDefineLimitAndStart() {

        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));

        Optional<Document> name = entity.find("name");
        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("name").eq(name.get().get())
                .start(2L)
                .limit(2L)
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertEquals(1, entities.size());

    }


    @SuppressWarnings("unused")
	@Test
    public void shouldSelectAll(){
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));


        DocumentQuery query = select().from(COLLECTION_NAME).build();
        Optional<Document> name = entity.find("name");
        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertTrue(entities.size() >= 4);
    }


    @Test
    public void shouldFindDocumentByName() {
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id4)
                , Document.of("name", "name3")));

        Optional<Document> name = entity.find("name");
        DocumentQuery query = select().from(COLLECTION_NAME)
        		.where("name").eq(name.get().get())
        		.build();
        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }

    @SuppressWarnings("unused")
    @Test
    public void shouldFindDocumentByNameSortAsc() {
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id4)
                , Document.of("name", "name3")));

        Optional<Document> name = entity.find("name");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .orderBy("name").asc()
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        List<String> result = entities.stream().flatMap(e -> e.getDocuments().stream())
                .filter(d -> "name".equals(d.getName()))
                .map(d -> d.get(String.class))
                .collect(Collectors.toList());

        assertFalse(result.isEmpty());
        assertThat(result, contains("name", "name", "name", "name3"));
    }

    @SuppressWarnings("unused")
    @Test
    public void shouldFindDocumentByNameSortDesc() {
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id4)
                , Document.of("name", "name3")));

        Optional<Document> name = entity.find("name");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .orderBy("name").desc()
                .build();
        List<DocumentEntity> entities = entityManager.select(query);
        List<String> result = entities.stream().flatMap(e -> e.getDocuments().stream())
                .filter(d -> "name".equals(d.getName()))
                .map(d -> d.get(String.class))
                .collect(Collectors.toList());

        assertFalse(result.isEmpty());
        assertThat(result, contains("name3", "name", "name", "name"));
    }



    @Test
    public void shouldFindDocumentById() {
        DocumentEntity entity = DocumentEntity.of("person", asList(Document.of("_id", id1)
                , Document.of("name", "name")));
        Optional<Document> id = entity.find("_id");

        DocumentQuery query = select().from(COLLECTION_NAME)
                .where("_id").eq(id.get().get())
                .build();

        List<DocumentEntity> entities = entityManager.select(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }
}