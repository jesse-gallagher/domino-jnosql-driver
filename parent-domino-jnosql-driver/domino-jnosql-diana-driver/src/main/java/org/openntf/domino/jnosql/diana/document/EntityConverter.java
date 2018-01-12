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
import org.jnosql.diana.driver.ValueUtil;
import org.openntf.domino.Database;
import org.openntf.domino.DocumentCollection;

import com.ibm.commons.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

final class EntityConverter {
	/**
	 * The field used to store the UNID of the document during JSON
	 * serialization, currently "_id"
	 */
	public static final String ID_FIELD = "_id"; //$NON-NLS-1$
	/**
	 * The expected field containing the collection name of the document in
	 * Darwino, currently "form"
	 */
	// TODO consider making this the store ID
	public static final String NAME_FIELD = "Form"; //$NON-NLS-1$
	
	private static final Set<String> IGNORED_FIELDS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	static {
		IGNORED_FIELDS.add("$$Key");
		IGNORED_FIELDS.add("$UpdatedBy");
		IGNORED_FIELDS.add("$Created");
		IGNORED_FIELDS.add(NAME_FIELD);
	}

	private EntityConverter() {
	}

	static List<DocumentEntity> convert(Collection<String> keys, Database database) {
		return keys.stream().map(t -> {
			return database.getDocumentWithKey(t);
		}).filter(Objects::nonNull).map(doc -> {
			List<Document> documents = toDocuments(doc);
			String name = StringUtil.toString(doc.get(NAME_FIELD));
			return DocumentEntity.of(name, documents);
		}).collect(Collectors.toList());
	}
	
	static List<DocumentEntity> convert(String query, Database database) {
		return convert(database.search(query));
	}
	
	static List<DocumentEntity> convert(DocumentCollection docs) {
		return docs.stream()
				.map(doc -> {
					List<Document> documents = toDocuments(doc);
					String name = StringUtil.toString(doc.getItemValueString(NAME_FIELD));
					return DocumentEntity.of(name, documents);
				})
				.collect(Collectors.toList());
	}

	private static List<Document> toDocuments(org.openntf.domino.Document doc) {
		List<Document> result = new ArrayList<>();
		String key = doc.getItemValueString("$$Key");
		if(StringUtil.isEmpty(key)) {
			key = doc.getUniversalID();
		}
		result.add(Document.of(ID_FIELD, key));
		result.addAll(toDocuments((Map<String, Object>)doc));
		result = result.stream().filter(d -> !IGNORED_FIELDS.contains(d.getName())).collect(Collectors.toList());
		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<Document> toDocuments(Map<String, Object> map) {
		List<Document> documents = new ArrayList<>();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (Map.class.isInstance(value)) {
				documents.add(Document.of(key, toDocuments(Map.class.cast(value))));
			} else if (isADocumentIterable(value)) {
				List<List<Document>> subDocuments = new ArrayList<>();
				stream(Iterable.class.cast(value).spliterator(), false)
					.map(m -> toDocuments(Map.class.cast(m)))
					.forEach(e -> subDocuments.add((List<Document>)e));
				documents.add(Document.of(key, subDocuments));
			} else {
				documents.add(Document.of(key, value));
			}
		}
		return documents;
	}

	@SuppressWarnings("unchecked")
	private static boolean isADocumentIterable(Object value) {
		return Iterable.class.isInstance(value) && stream(Iterable.class.cast(value).spliterator(), false).allMatch(d -> Map.class.isInstance(d));
	}

	static Map<String, Object> convert(DocumentEntity entity) {
		requireNonNull(entity, "entity is required"); //$NON-NLS-1$

		Map<String, Object> jsonObject = new LinkedHashMap<>();
		entity.getDocuments().stream().forEach(toJsonObject(jsonObject));
		jsonObject.put(NAME_FIELD, entity.getName());
		jsonObject.remove(ID_FIELD);
		return jsonObject;
	}

	private static Consumer<Document> toJsonObject(Map<String, Object> jsonObject) {
        return d -> {
        		// Swap out sensitive names
            Object value = ValueUtil.convert(d.getValue());
            
            if (Document.class.isInstance(value)) {
                convertDocument(jsonObject, d, value);
            } else if (Iterable.class.isInstance(value)) {
                convertIterable(jsonObject, d, value);
            } else {
                jsonObject.put(d.getName(), value);
            }
        };
    }

	private static void convertDocument(Map<String, Object> jsonObject, Document d, Object value) {
		Document document = Document.class.cast(value);
		jsonObject.put(d.getName(), Collections.singletonMap(document.getName(), document.get()));
	}

	@SuppressWarnings("unchecked")
	private static void convertIterable(Map<String, Object> jsonObject, Document document, Object value) {
		Map<String, Object> map = new LinkedHashMap<>();
		List<Object> array = new ArrayList<>();
		Iterable.class.cast(value).forEach(element -> {
			if(Document.class.isInstance(element)) {
				Document subDocument = Document.class.cast(element);
				map.put(subDocument.getName(), subDocument.get());
			} else if(isSubDocument(element)) {
				Map<String, Object> subJson = new LinkedHashMap<>();
				stream(Iterable.class.cast(element).spliterator(), false)
					.forEach(getSubDocument(subJson));
				array.add(subJson);
			} else {
				array.add(element);
			}
		});
		if(array.isEmpty()) {
			jsonObject.put(document.getName(), map);
		} else {
			jsonObject.put(document.getName(), array);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static Consumer getSubDocument(Map<String, Object> subJson) {
		return e -> toJsonObject(subJson).accept((Document)e);
	}
	
	@SuppressWarnings("unchecked")
	private static boolean isSubDocument(Object value) {
		return value instanceof Iterable && stream(Iterable.class.cast(value).spliterator(), false)
				.allMatch(d -> org.jnosql.diana.api.document.Document.class.isInstance(d));
	}
}