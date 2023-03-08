/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.schema.impl;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.persistence.metamodel.Attribute;

class PredicateFilter implements Comparable<PredicateFilter>, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public enum Criteria {

        /**
         * less than (numbers, dates)
         */
        LT,
        /**
         * greater than (numbers, dates)
         */
        GT,
        /**
         * less or equal (numbers, dates)
         */
        LE,
        /**
         * greater or equal (numbers, dates) DEFAULT for dates and floating
         * point numbers
         */
        GE,
        /**
         * equal (numbers, dates, booleans) DEFAULT for natural numbers and
         * booleans
         */
        EQ,
        /**
         * not equal (numbers, dates, booleans, strings)
         */
        NE,
        /**
         * not equal strings, case insensitive
         */
        NE_,
        /**
         * case sensitive (strings)
         */
        CASE,
        /**
         * case insensitive match
         * <pre>LOWER(field) = LOWER(SEARCH)</pre> 
         */
        LOWER, EQ_,
        /**
         * end of the string matches, case sensitive
         */
        ENDS,
        /**
         * end of the string matches, case insensitive
         */
        ENDS_,
        /**
         * beginning of string matches, case sensitive
         * <pre>LIKE SEARCH%</pre>
         */
        STARTS,
        /**
         * beginning of string matches, case insensitive   
         * <pre>LIKE SEARCH%</pre>
         */
        STARTS_,
        /**
         * any part of the string matches, case sensitive
         * <pre>LIKE %SEARCH%</pre>
         */
        LIKE,
        /**
         * any part of the string matches, case insensitive   
         * <pre>LIKE %SEARCH%</pre>
         */
        LIKE_,
        /**
         * full string match =
         */
        EXACT,
        /**
         * Is Null condition
         */
        IS_NULL,
        /**
         * Is Not Null condition
         */
        NOT_NULL,
        /**
         * IN condition
         */
        IN,
        /**
         * Not In condition
         */
        NIN,
        /**
         * Between condition
         */
        BETWEEN,
        /**
         * Not Between condition
         */
        NOT_BETWEEN,

        /**
         * JPA's LOCATE predicate for attributes annotated with @Convert
         */
        LOCATE;
        
        private static Set<String> names = EnumSet.allOf(Criteria.class)
                                                  .stream()
                                                  .map(it -> it.name().toString())
                                                  .collect(Collectors.toSet());
        
        public static Set<String> names() {
            return names;
        }
    }

    private final String field;

    private final Object typedValue;

    private final EnumSet<Criteria> criterias;

    private final Attribute attribute;

    public PredicateFilter(String field, Object value, Set<Criteria> criterias, Attribute attribute) {
        this.field = field;
        this.typedValue = value;
        this.criterias = EnumSet.copyOf(criterias);
        this.attribute = attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Class<?> getJavaType() {
        return attribute.getJavaType();
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return typedValue;
    }

    public Set<Criteria> getCriterias() {
        return EnumSet.copyOf(criterias);
    }

    @Override
    public int compareTo(PredicateFilter o) {
        return this.getField().compareTo(o.getField());
    }
    
    public boolean anyMatch(Criteria...criterias) {
        return Stream.of(criterias)
                     .anyMatch(criteria -> this.getCriterias()
                                               .contains(criteria));
    }    
}
