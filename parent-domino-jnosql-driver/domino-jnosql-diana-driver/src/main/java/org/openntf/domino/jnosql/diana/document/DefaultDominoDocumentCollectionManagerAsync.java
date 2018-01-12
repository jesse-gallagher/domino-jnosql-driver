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

import org.jnosql.diana.api.ExecuteAsyncQueryException;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;

import rx.functions.Action1;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static rx.Observable.just;

class DefaultDominoDocumentCollectionManagerAsync implements DominoDocumentCollectionManagerAsync {

	private static final Consumer<DocumentEntity> NOOP = d -> {
	};
	private static final Action1<Throwable> ERROR_SAVE = a -> new ExecuteAsyncQueryException("On error when try to execute Darwino save method");
	private static final Action1<Throwable> ERROR_FIND = a -> new ExecuteAsyncQueryException("On error when try to execute Darwino find method");
	private static final Action1<Throwable> ERROR_DELETE = a -> new ExecuteAsyncQueryException("On error when try to execute Darwino delete method");
	private static final Action1<Throwable> ERROR_QUERY = a -> new ExecuteAsyncQueryException("On error when try to execute Darwino query method");

	private final DominoDocumentCollectionManager manager;

	DefaultDominoDocumentCollectionManagerAsync(DominoDocumentCollectionManager manager) {
		this.manager = manager;
	}

	@Override
	public void insert(DocumentEntity entity) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		insert(entity, NOOP);
	}

	@Override
	public void insert(DocumentEntity entity, Duration ttl) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		insert(entity, ttl, NOOP);
	}

	@Override
	public void insert(DocumentEntity entity, Consumer<DocumentEntity> callBack) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		requireNonNull(callBack, "callBack is required");
		just(entity).map(manager::insert).subscribe(callBack::accept, ERROR_SAVE);
	}

	@Override
	public void insert(DocumentEntity entity, Duration ttl, Consumer<DocumentEntity> callBack)
			throws ExecuteAsyncQueryException, UnsupportedOperationException {
		requireNonNull(callBack, "callBack is required");
		just(entity).map(e -> manager.insert(e, ttl)).subscribe(callBack::accept, ERROR_SAVE);
	}

	@Override
	public void update(DocumentEntity entity) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		insert(entity);
	}

	@Override
	public void update(DocumentEntity entity, Consumer<DocumentEntity> callBack) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		insert(entity, callBack);
	}

	@Override
	public void delete(DocumentDeleteQuery query) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		delete(query, v -> {
		});
	}

	@Override
	public void delete(DocumentDeleteQuery query, Consumer<Void> callBack) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		requireNonNull(query, "query is required");
		requireNonNull(callBack, "callBack is required");
		just(query).map(q -> {
			manager.delete(q);
			return true;
		}).subscribe(a -> callBack.accept(null), ERROR_DELETE);
	}

	@Override
	public void select(DocumentQuery query, Consumer<List<DocumentEntity>> callBack) throws ExecuteAsyncQueryException, UnsupportedOperationException {
		just(query).map(manager::select).subscribe(callBack::accept, ERROR_FIND);
	}

	@Override
	public void query(String query, Map<String, Object> params, Consumer<List<DocumentEntity>> callback)
			throws NullPointerException, ExecuteAsyncQueryException {
		requireNonNull(callback, "callback is required");
		just(query).map(n -> manager.query(n, params)).subscribe(callback::accept, ERROR_QUERY);
	}

	@Override
	public void query(String query, Consumer<List<DocumentEntity>> callback) throws NullPointerException, ExecuteAsyncQueryException {
		requireNonNull(callback, "callback is required");
		just(query).map(manager::query).subscribe(callback::accept, ERROR_QUERY);
	}

	@Override
	public void close() {
		manager.close();
	}

	@Override
	public void search(String query, Consumer<List<DocumentEntity>> callback) throws ExecuteAsyncQueryException {
		requireNonNull(callback, "callback is required");
		just(query).map(manager::search).subscribe(callback::accept, ERROR_QUERY);
	}
}