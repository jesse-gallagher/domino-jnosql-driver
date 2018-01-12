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

import org.apache.commons.text.StrSubstitutor;
import org.jnosql.diana.api.Sort;
import org.jnosql.diana.api.Sort.SortType;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.openntf.domino.Database;
import org.openntf.domino.Item;
import org.openntf.domino.DocumentCollection;

import com.ibm.commons.util.StringUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.openntf.domino.jnosql.diana.document.EntityConverter.convert;

/**
 * The default implementation of {@link DominoDocumentCollectionManager}
 */
class DefaultDominoDocumentCollectionManager implements DominoDocumentCollectionManager {
	private final Database database;

	DefaultDominoDocumentCollectionManager(Database database) {
		this.database = database;
	}

	@Override
	public DocumentEntity insert(DocumentEntity entity) {
		requireNonNull(entity, "entity is required");
		Map<String, Object> jsonObject = convert(entity);
		Optional<Document> maybeId = entity.find(EntityConverter.ID_FIELD);
		Document id;
		if(maybeId.isPresent()) {
			id = maybeId.get();
		} else {
			// Auto-insert a UNID
			id = Document.of(EntityConverter.ID_FIELD, UUID.randomUUID().toString());
			entity.add(id);
		}

		String unid = StringUtil.toString(id.get());
		org.openntf.domino.Document doc = database.getDocumentWithKey(unid, true);
		doc.putAll(jsonObject);
		doc.save();
		if(database.isFTIndexed()) {
			database.updateFTIndex(false);
		}
		return entity;
	}

	@Override
	public DocumentEntity insert(DocumentEntity entity, Duration ttl) {
		requireNonNull(entity, "entity is required");
		requireNonNull(ttl, "ttl is required");
		return insert(entity);
	}

	@Override
	public DocumentEntity update(DocumentEntity entity) {
		Map<String, Object> jsonObject = convert(entity);
		Document id = entity.find(EntityConverter.ID_FIELD).orElseThrow(() -> new DominoNoKeyFoundException(entity.toString()));

		String unid = StringUtil.toString(id.get());
		org.openntf.domino.Document doc = database.getDocumentWithKey(unid);
		doc.putAll(jsonObject);
		// Remove items that are no longer in the map
		Set<String> keys = jsonObject.keySet();
		for(Item item : doc.getItems()) {
			if(!keys.contains(item.getName())) {
				item.remove();
			}
		}
		doc.save();
		if(database.isFTIndexed()) {
			database.updateFTIndex(false);
		}
		return entity;
	}

	@Override
	public void delete(DocumentDeleteQuery query) {
		String delete = QueryConverter.delete(query);
		if(StringUtil.isNotEmpty(delete)) {
			DocumentCollection docs = database.search(delete);
			docs.removeAll(false);
		}
		if(database.isFTIndexed()) {
			database.updateFTIndex(false);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DocumentEntity> select(DocumentQuery query) throws NullPointerException {
		String select = QueryConverter.select(query);
		List<DocumentEntity> entities = new ArrayList<>();
		if (StringUtil.isNotEmpty(select)) {
			entities.addAll(convert(select, database));
		}
		
		// Do this the hard way
		List<Sort> sorts = query.getSorts();
		if(sorts != null) {
			for(Sort sort : sorts) {
				Collections.sort(entities, (a, b) -> {
					String name = sort.getName();
					Optional<Document> docA = a.find(name);
					Optional<Document> docB = b.find(name);
					
					if(!docA.isPresent() && !docB.isPresent()) {
						return 0;
					}
					
					SortType type = sort.getType();
					if(type == null) { type = SortType.ASC; }
					switch(type) {
					case DESC:
						if(!docA.isPresent()) {
							return 1;
						} else if(!docB.isPresent()) {
							return 0;
						} else {
							Comparable<Object> valA = docA.get().get(Comparable.class);
							Comparable<Object> valB = docB.get().get(Comparable.class);
							return -1 * valA.compareTo(valB);
						}
					case ASC:
					default:
						if(!docA.isPresent()) {
							return 0;
						} else if(!docB.isPresent()) {
							return 1;
						} else {
							Comparable<Object> valA = docA.get().get(Comparable.class);
							Comparable<Object> valB = docB.get().get(Comparable.class);
							return valA.compareTo(valB);
						}
					}
				});
			}
		}
		
		// Limit similarly
		return entities.stream()
			.skip(query.getFirstResult())
			.limit(query.getMaxResults() < 1 ? entities.size() : query.getMaxResults())
			.collect(Collectors.toList());
	}

	@Override
	public List<DocumentEntity> query(String query, Map<String, Object> params) throws NullPointerException {
		requireNonNull(query, "query is required");
		requireNonNull(params, "params is required");
		return convert(StrSubstitutor.replace(query, params), database);
	}

	@Override
	public List<DocumentEntity> query(String query) throws NullPointerException {
		requireNonNull(query, "query is required");
		return convert(query, database);
	}

	@Override
	public List<DocumentEntity> search(String query) {
		return convert(database.FTSearch(query));
	}

	@Override
	public void close() {

	}

}