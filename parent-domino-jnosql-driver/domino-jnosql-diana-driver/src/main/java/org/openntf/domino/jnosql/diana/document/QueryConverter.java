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

import org.jnosql.diana.api.Condition;
import org.jnosql.diana.api.Sort.SortType;
import org.jnosql.diana.api.TypeReference;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCondition;
import org.jnosql.diana.api.document.DocumentDeleteQuery;
import org.jnosql.diana.api.document.DocumentQuery;
import org.openntf.domino.utils.DominoUtils;

import com.ibm.commons.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jnosql.diana.api.Condition.IN;

/**
 * Assistant class to convert queries from Diana internal structures to Darwino
 * cursors.
 * 
 * @author Jesse Gallagher
 * @since 0.0.4
 */
final class QueryConverter {

	private static final Set<Condition> NOT_APPENDABLE = EnumSet.of(IN, Condition.AND, Condition.OR);

	private static final String[] ALL_SELECT = { "@All" }; //$NON-NLS-1$

	private QueryConverter() {
	}

	static String select(DocumentQuery query) {
		String[] documents = query.getDocuments().stream().toArray(size -> new String[size]);
		if (documents.length == 0) {
			documents = ALL_SELECT;
		}

		// TODO limits
		int firstResult = (int) query.getFirstResult();
		int maxResult = (int) query.getMaxResults();

		StringBuilder result = new StringBuilder();
		// TODO ordering
		String[] sorts = query.getSorts().stream().map(s -> s.getName() + (s.getType() == SortType.DESC ? " d" : "")).toArray(String[]::new); //$NON-NLS-1$ //$NON-NLS-2$

		if (query.getCondition().isPresent()) {
			applyCollectionName(result, query.getDocumentCollection());
			if(result.length() != 0) {
				result.append(" & ");
			}
			result.append(getCondition(query.getCondition().get()));
		} else {
			applyCollectionName(result, query.getDocumentCollection());
		}
		return result.toString();
	}

	static String delete(DocumentDeleteQuery query) {
		StringBuilder result = new StringBuilder();
		applyCollectionName(result, query.getDocumentCollection());
		if(result.length() != 0) {
			result.append(" & ");
		}
		result.append(getCondition(query.getCondition().orElseThrow(() -> new IllegalArgumentException("Condition is required"))));
		return result.toString();
	}

	private static String getCondition(DocumentCondition condition) {
		Document document = condition.getDocument();

		// Convert special names
		String name = document.getName();
		if (StringUtil.equals(name, EntityConverter.ID_FIELD)) {
			name = "@Text(@DocumentUniqueID)";
		}

		String placeholder = toFormulaEntity(document.get());
		switch (condition.getCondition()) {
		case EQUALS:
			return infix("=", name, placeholder);
		case LESSER_THAN:
			return infix("<", name, placeholder);
		case LESSER_EQUALS_THAN:
			return infix("<=", name, placeholder);
		case GREATER_THAN:
			return infix(">", name, placeholder);
		case GREATER_EQUALS_THAN:
			return infix(">=", name, placeholder);
		case LIKE:
			return "@Like(" + name + ";" + placeholder + ")";
		case IN:
			return "@Contains(" + name + ";" + placeholder + ")";
		case AND:
			return infix(" & ", document.get(new TypeReference<List<DocumentCondition>>() {
			}).stream().map(d -> getCondition(d)).filter(Objects::nonNull).toArray());
		case OR:
			return infix(" | ", document.get(new TypeReference<List<DocumentCondition>>() {
			}).stream().map(d -> getCondition(d)).filter(Objects::nonNull).toArray());
		case NOT:
			DocumentCondition dc = document.get(DocumentCondition.class);
			return "!" + getCondition(dc);
		default:
			throw new IllegalStateException("This condition is not supported in Darwino: " + condition.getCondition());
		}
	}
	
	private static String infix(String op, Object... components) {
		return "(" + Arrays.asList(components).stream().map(StringUtil::toString).collect(Collectors.joining(op)) + ")";
	}
	
	@SuppressWarnings("unchecked")
	private static String toFormulaEntity(Object obj) {
		if(obj instanceof Number) {
			return obj.toString();
			// TODO dates
		} else if(obj instanceof Collection) {
			return (String)Collection.class.cast(obj).stream().map(QueryConverter::toFormulaEntity).collect(Collectors.joining(":"));
		} else if(obj == null) {
			return "\"\"";
		} else {
			return '"' + DominoUtils.escapeForFormulaString(obj.toString()) + "'";
		}
	}
	
	private static void applyCollectionName(StringBuilder condition, String collection) {
		if(StringUtil.isNotEmpty(collection)) {
			if(condition.length() != 0) {
				condition.append(" & ");
			}
			condition.append("Form=\"" + DominoUtils.escapeForFormulaString(collection) + "\"");
		}
	}
}
