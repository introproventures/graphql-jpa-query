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

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;

import com.introproventures.graphql.jpa.query.schema.impl.PredicateFilter.Criteria;
import graphql.language.NullValue;

/**
 * Supported types to build predicates for
 *
 * <ul>
 * <li> Boolean </li>
 * <li> Byte </li>
 * <li> Short </li>
 * <li> Character </li>
 * <li> Integer </li>
 * <li> Long </li>
 * <li> Float </li>
 * <li> Double </li>
 * <li> java.math.BigInteger </li>
 * <li> java.math.BigDecimal </li>
 * <li> java.lang.String </li>
 * <li> java.util.Date </li>
 * <li> java.time.LocalDate </li>
 * <li> java.time.LocalDateTime </>
 * <li> java.time.Instant </li>
 * <li> java.time.LocalTime </li>
 * <li> java.util.Calendar </li>
 * <li> java.sql.Date </li>
 * <li> java.sql.Time </li>
 * <li> java.sql.Timestamp </li>
 * </ul>
 *
 */
class JpaPredicateBuilder {

    private final CriteriaBuilder cb;

    private final EnumSet<Logical> globalOptions;

    /**
     * Field name can be prepended with (comma separated list of local options)
     * if field is prepended with (), even without options, any global options
     * avoided and defaults are used.Defaults: for numbers and booleas -
     * equality; for strings case insensitive beginning matches; for dates
     * greater or equal;
     *
     * @param cb
     * @param globalOptions
     */
    public JpaPredicateBuilder(CriteriaBuilder cb, EnumSet<Logical> globalOptions) {
        this.cb = cb;
        this.globalOptions = globalOptions;
    }

    protected Predicate addOrNull(Path<?> root, Predicate p) {
        Predicate pr = cb.isNull(root);
        return cb.or(p, pr);
    }

    /**
     *
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getStringPredicate(Path<String> root, PredicateFilter filter) {
        Expression<String> fieldValue;

        // list or arrays only for in and not in, between and not between
        Predicate arrayValuePredicate = mayBeArrayValuePredicate(root, filter);

        if(arrayValuePredicate == null) {
            String compareValue = filter.getValue().toString();

            if (filter.getCriterias().contains(PredicateFilter.Criteria.IN)) {
                CriteriaBuilder.In<Object> in = cb.in(root);
                return in.value(compareValue);
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                return cb.not(root.in(compareValue));
            }

            if (filter.getCriterias().contains(PredicateFilter.Criteria.CASE)) {
                fieldValue = root;
            }
            else {
                fieldValue = cb.lower(root);
                compareValue = compareValue.toLowerCase();
            }

            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(fieldValue, compareValue);
            } 
            else if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(fieldValue, compareValue);
            }
            else if (filter.getCriterias().contains(PredicateFilter.Criteria.LIKE)) {
                compareValue = "%" + compareValue + "%";
            }
            else if (filter.getCriterias().contains(PredicateFilter.Criteria.ENDS)) {
                compareValue = "%" + compareValue;
            }
            else if (filter.getCriterias().contains(PredicateFilter.Criteria.EXACT)) {
                // do nothing
            }
            // STARTS or empty (default)
            else {
                compareValue = compareValue + "%";
            }
            return cb.like(fieldValue, compareValue);
        }

        return arrayValuePredicate;
    }

    protected Predicate getBooleanPredicate(Path<?> root, PredicateFilter filter) {
        Boolean bool = (Boolean) filter.getValue();
        if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
            bool = !bool;
        }
        return cb.equal(root, bool);
    }

    /**
     * Do not confuse with Java type Integer, it is for all types without
     * fractional component: BigInteger, Long, Integer, Short, Byte
     *
     * @param root
     * @param filter
     * @return
     */
    protected Predicate getIntegerPredicate(Path<? extends Number> root, PredicateFilter filter) {

        // list or arrays only for in and not in
        Predicate arrayValuePredicate = mayBeArrayValuePredicate(root, filter);

        if (arrayValuePredicate == null && filter.getValue() != null && filter.getValue() instanceof Number) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.IN)) {
                CriteriaBuilder.In<Object> in = cb.in(root);
                return in.value(filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                return cb.not(root.in(filter.getValue()));
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lt(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.gt(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LE)) {
                return cb.le(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.ge(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            return cb.equal(root, filter.getValue());
        }
        return arrayValuePredicate;
    }

    protected Predicate mayBeArrayValuePredicate(Path<?> path, PredicateFilter filter) {
        // arrays only for in
        if (filter.getValue().getClass().isArray()) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                && !filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                CriteriaBuilder.In<Object> in = cb.in(path);
                for(Object n : (Object[]) filter.getValue()) {
                    in.value(n);
                }
                return in;
            } else if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                || filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                return cb.not(path.in((Object[]) filter.getValue()));
            } else if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values = (Object[]) filter.getValue();
                if (values.length == 2) {
                    Expression<String> name = path.get(filter.getField());
                    Predicate between = cb.between(name, cb.literal((String) values[0]), cb.literal((String) values[1]));
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        } else if ((filter.getValue() instanceof Collection)) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                && !filter.getCriterias().contains(PredicateFilter.Criteria.NIN) &&
                !(filter.getCriterias().contains(Criteria.NOT_BETWEEN) || filter.getCriterias().contains(Criteria.BETWEEN))) {
                CriteriaBuilder.In<Object> in = cb.in(path);
                for(Object n : (Collection<?>) filter.getValue()) {
                    in.value(n);
                }
                return in;
            } else if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                || filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                return cb.not(path.in((Collection<?>) filter.getValue()));
            } else if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.NOT_BETWEEN) || filter.getCriterias().contains(Criteria.BETWEEN))) {
                Expression name = (Expression) path;
                Collection<?> collection = (Collection<?>) filter.getValue();
                if (collection.size() == 2) {
                    Object[] values = collection.toArray();
                    Expression fromValue = cb.literal(values[0]);
                    Expression toValue = cb.literal(values[1]);
                    Predicate between = cb.between(name, fromValue, toValue);
                    if (filter.getCriterias().contains(Criteria.BETWEEN)) {
                        return between;
                    }

                    return cb.not(between);
                }
            }
        }

        return null;

    }

    protected Predicate getFloatingPointPredicate(Path<? extends Number> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof Number) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lt(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.gt(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.ge(root, (Number) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.le(root, (Number) filter.getValue());
        }
        return null;
    }

    // TODO: other date types
    protected Predicate getDatePredicate(Path<? extends Date> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof Date) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (Date) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (Date) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (Date) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (Date) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<Date> name = (Expression<Date>) root;
                    Expression<Date> fromDate = cb.literal((Date) values[0]);
                    Expression<Date> toDate = cb.literal((Date) values[1]);
                    Predicate between = cb.between(name, fromDate, toDate);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    protected Predicate getLocalDatePredicate(Path<? extends LocalDate> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof LocalDate) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (LocalDate) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (LocalDate) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (LocalDate) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (LocalDate) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<LocalDate> name = (Expression<LocalDate>) root;
                    Expression<LocalDate> fromDate = cb.literal((LocalDate) values[0]);
                    Expression<LocalDate> toDate = cb.literal((LocalDate) values[1]);
                    Predicate between = cb.between(name, fromDate, toDate);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    protected Predicate getLocalDateTimePredicate(Path<? extends LocalDateTime> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof LocalDateTime) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (LocalDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (LocalDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (LocalDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (LocalDateTime) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<LocalDateTime> name = (Expression<LocalDateTime>) root;
                    Expression<LocalDateTime> fromDateTime = cb.literal((LocalDateTime) values[0]);
                    Expression<LocalDateTime> toDateTime = cb.literal((LocalDateTime) values[1]);
                    Predicate between = cb.between(name, fromDateTime, toDateTime);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    protected Predicate getInstantPredicate(Path<? extends Instant> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof Instant) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (Instant) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (Instant) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (Instant) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (Instant) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<Instant> name = (Expression<Instant>) root;
                    Expression<Instant> fromDate = cb.literal((Instant) values[0]);
                    Expression<Instant> toDate = cb.literal((Instant) values[1]);
                    Predicate between = cb.between(name, fromDate, toDate);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    protected Predicate getLocalTimePredicate(Path<? extends LocalTime> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof LocalTime) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (LocalTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (LocalTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (LocalTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (LocalTime) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<LocalTime> name = (Expression<LocalTime>) root;
                    Expression<LocalTime> fromTime = cb.literal((LocalTime) values[0]);
                    Expression<LocalTime> toTime = cb.literal((LocalTime) values[1]);
                    Predicate between = cb.between(name, fromTime, toTime);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    protected Predicate getZonedDateTimePredicate(Path<? extends ZonedDateTime> root, PredicateFilter filter) {
        if (filter.getValue() != null && filter.getValue() instanceof ZonedDateTime) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LT)) {
                return cb.lessThan(root, (ZonedDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GT)) {
                return cb.greaterThan(root, (ZonedDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.GE)) {
                return cb.greaterThanOrEqualTo(root, (ZonedDateTime) filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
                return cb.equal(root, filter.getValue());
            }
            if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                return cb.notEqual(root, filter.getValue());
            }
            // LE or default
            return cb.lessThanOrEqualTo(root, (ZonedDateTime) filter.getValue());
        } else if (filter.getValue().getClass().isArray() || filter.getValue() instanceof Collection) {
            if (!filter.getCriterias().contains(PredicateFilter.Criteria.NE)
                    && (filter.getCriterias().contains(Criteria.BETWEEN) || filter.getCriterias().contains(Criteria.NOT_BETWEEN))) {

                Object[] values;
                if (filter.getValue().getClass().isArray()) {
                    values = (Object[]) filter.getValue();
                } else {
                    values = ((Collection<?>) filter.getValue()).toArray();
                }

                if (values.length == 2) {
                    Expression<ZonedDateTime> name = (Expression<ZonedDateTime>) root;
                    Expression<ZonedDateTime> fromDateTime = cb.literal((ZonedDateTime) values[0]);
                    Expression<ZonedDateTime> toDateTime = cb.literal((ZonedDateTime) values[1]);
                    Predicate between = cb.between(name, fromDateTime, toDateTime);
                    if (filter.getCriterias().contains(Criteria.BETWEEN))
                        return between;
                    return cb.not(between);
                }
            }
        }
        return null;
    }

    private Predicate getUuidPredicate(Path<UUID> field, PredicateFilter filter) {
        if (filter.getValue() == null) {
            return null;
        }
        final Predicate arrayPredicate = mayBeArrayValuePredicate(field, filter);
        if (arrayPredicate != null) {
            return arrayPredicate;
        }
        final UUID compareValue = (UUID) filter.getValue();

        if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
            return cb.equal(field, compareValue);
        }
        if (filter.getCriterias().contains(PredicateFilter.Criteria.IN)) {
            final CriteriaBuilder.In<Object> in = cb.in(field);
            return in.value(compareValue);
        }
        if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
            return cb.notEqual(field, compareValue);
        }
        return null;
    }
    
    private Predicate getEnumPredicate(Path<? extends Enum> field, PredicateFilter filter) {
        if (filter.getValue() == null) {
            return null;
        }
        final Predicate arrayPredicate = mayBeArrayValuePredicate(field, filter);
        
        if (arrayPredicate != null) {
            return arrayPredicate;
        }
        final Enum<?> compareValue = (Enum<?>) filter.getValue();

        if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)) {
            return cb.equal(field, compareValue);
        }
        if (filter.getCriterias().contains(PredicateFilter.Criteria.IN)) {
            final CriteriaBuilder.In<Object> in = cb.in(field);
            return in.value(compareValue);
        }
        if (filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
            final CriteriaBuilder.In<Object> in = cb.in(field);
            return cb.not(in.value(compareValue));
        }
        if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
            return cb.notEqual(field, compareValue);
        }
        return null;
    }    

    @SuppressWarnings("unchecked")
    private Predicate getTypedPredicate(From<?,?> from, Path<?> field, PredicateFilter filter) {
        Class<?> type = field.getJavaType();
        Object value = filter.getValue();
        Set<Criteria> criterias = filter.getCriterias();

        if(value == null) {
            return cb.disjunction();
        }

        if(criterias.contains(Criteria.IS_NULL)) {
            return (boolean) value ? cb.isNull(field) : cb.isNotNull(field);
        } else if (criterias.contains(Criteria.NOT_NULL)) {
            return (boolean) value ? cb.isNotNull(field) : cb.isNull(field) ;
        }

        PredicateFilter predicateFilter = new PredicateFilter(filter.getField(), value, criterias);

        if (type.isPrimitive())
            type = JpaQueryBuilder.PRIMITIVES_TO_WRAPPERS.get(type);
        if (type.equals(String.class)) {
            return getStringPredicate((Path<String>)field, filter);
        }
        else if (type.equals(Long.class)
                || type.equals(BigInteger.class)
                || type.equals(Integer.class)
                || type.equals(Short.class)
                || type.equals(Byte.class)) {
            return getIntegerPredicate((Path<Number>) field, predicateFilter);
        }
        else if (type.equals(BigDecimal.class)
                || type.equals(Double.class)
                || type.equals(Float.class)) {
            return getFloatingPointPredicate((Path<Number>) field, predicateFilter);
        }
        else if (type.equals(java.util.Date.class)) {
            return getDatePredicate((Path<Date>) field, predicateFilter);
        }
        else if(type.equals(java.time.LocalDate.class)){
            return getLocalDatePredicate((Path<LocalDate>) field, predicateFilter);
        }
        else if(type.equals(LocalDateTime.class)){
            return getLocalDateTimePredicate((Path<LocalDateTime>) field, predicateFilter);
        }
        else if(type.equals(Instant.class)){
            return getInstantPredicate((Path<Instant>) field, predicateFilter);
        }
        else if(type.equals(LocalTime.class)){
            return getLocalTimePredicate((Path<LocalTime>) field, predicateFilter);
        }
        else if(type.equals(ZonedDateTime.class)){
            return getZonedDateTimePredicate((Path<ZonedDateTime>) field, predicateFilter);
        }
        else if (type.equals(Boolean.class)) {
            return getBooleanPredicate(field, predicateFilter);
        }
        else if (type.equals(UUID.class)) {
            return getUuidPredicate((Path<UUID>) field, predicateFilter);
        }
        else if(Collection.class.isAssignableFrom(type)) {
            // collection join for plural attributes
            if(PluralAttribute.class.isInstance(from.getModel()) 
                    || EntityType.class.isInstance(from.getModel())) {
                From<?,?> join = null;
                
                for(Join<?,?> fetch: from.getJoins()) {
                    if(fetch.getAttribute().getName().equals(filter.getField()))
                        join = (From<?,?>) fetch;
                }
                
                if(join == null) {
                    join = (From<?,?>) from.join(filter.getField());
                }
                
                
                Predicate in = join.in(value);
                
                if(filter.getCriterias().contains(PredicateFilter.Criteria.NIN))
                   return cb.not(in);
                        
                return in;
            }
        } else if(type.isEnum()) {
        	return getEnumPredicate((Path<Enum<?>>) field, predicateFilter);
        } // TODO need better detection mechanism
        else if (Object.class.isAssignableFrom(type)) {
            if (filter.getCriterias().contains(PredicateFilter.Criteria.LOCATE)) {
                return cb.gt(cb.locate(from.<String>get(filter.getField()), value.toString()), 0); 
            }
            else { 
                Object object = value;
                
                if(Collection.class.isInstance(value)) {
                    object = getValues(object, type);
                } else {
                    object = getValue(object, type);
                }
                    
                if (filter.getCriterias().contains(PredicateFilter.Criteria.EQ)
                        || filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                    
                    Predicate equal = cb.equal(from.get(filter.getField()), object);
                    
                    if (filter.getCriterias().contains(PredicateFilter.Criteria.NE)) {
                        return cb.not(equal);
                    }
                    
                    return equal;
                } 
                else if (filter.getCriterias().contains(PredicateFilter.Criteria.IN) 
                        || filter.getCriterias().contains(PredicateFilter.Criteria.NIN)
                ) {
                    CriteriaBuilder.In<Object> in = cb.in(from.get(filter.getField()));
                    
                    if(Collection.class.isInstance(object)) {
                        Collection.class.cast(object)
                                  .forEach(in::value);
                    } else {
                        in.value(object);
                    }
                    
                    if(filter.getCriterias().contains(PredicateFilter.Criteria.NIN)) {
                        return cb.not(in);
                    }
                    
                    return in;
                } 
            }
        }

        throw new IllegalArgumentException("Unsupported field type " + type + " for field " + predicateFilter.getField());
    }

    private Object getValue(Object object, Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructor(Object.class);
            if(constructor != null) {
                Object arg = NullValue.class.isInstance(object) ? null : object;
                return constructor.newInstance(arg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return object;
    }
    
    private Object getValues(Object object, Class<?> type) {
        Collection<Object> objects = new ArrayList<>();

        for (Object value : Collection.class.cast(object).toArray()) {
            objects.add(getValue(value, type));
        }

        return objects;
    }
    
    /**
     * Makes predicate for field of primitive type
     *
     * @throws IllegalArgumentException if field type is not primitive or
     * unsupported
     * @param field
     * @param filter
     * @return constructed predicate, returns null if value cannot be converted
     * to field type
     */
    public Predicate getPredicate(From<?,?> from, Path<?> field, PredicateFilter filter) {
        return getTypedPredicate(from, field, filter);
    }
}
