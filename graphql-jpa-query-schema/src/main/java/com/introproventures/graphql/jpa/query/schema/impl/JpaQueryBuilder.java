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
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.EnumSet;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.persistence.EntityManager;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.From;
//import javax.persistence.criteria.Join;
//import javax.persistence.criteria.JoinType;
//import javax.persistence.criteria.Order;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import javax.persistence.metamodel.Attribute;
//import javax.persistence.metamodel.EntityType;
//import javax.persistence.metamodel.Metamodel;
//import javax.persistence.metamodel.PluralAttribute;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.introproventures.graphql.jpa.query.annotation.GraphQLDefaultOrderBy;
//
///**
// * Jpa Criteria Query Builder class used to apply predicate filters and orders and build Criteria Query.  
// * 
// * @param <T>
// * @todo make inner joins for compound fields garanteed to have joined data
// */
//class JpaQueryBuilder<T> {
//
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private final Class<T> clazz;
//
//    private final CriteriaBuilder criteriaBuilder;
//
//    private final JpaPredicateBuilder predicateBuilder;
//
//    private final EnumSet<Logical> options;
//
//    private final Metamodel metamodel;
//
//    private final List<PredicateFilter> filters = new ArrayList<>();
//
//    private final LinkedHashMap<String, Boolean> orders = new LinkedHashMap<>();
//
//    public static final Map<Class<?>, Class<?>> WRAPPERS_TO_PRIMITIVES = new HashMap<Class<?>, Class<?>>();
//    public static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>();
//
//    static {
//        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
//        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
//        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
//        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
//        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
//        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
//        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
//        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
//        PRIMITIVES_TO_WRAPPERS.put(void.class, Void.class);
//
//        WRAPPERS_TO_PRIMITIVES.put(Boolean.class, boolean.class);
//        WRAPPERS_TO_PRIMITIVES.put(Byte.class, byte.class);
//        WRAPPERS_TO_PRIMITIVES.put(Character.class, char.class);
//        WRAPPERS_TO_PRIMITIVES.put(Double.class, double.class);
//        WRAPPERS_TO_PRIMITIVES.put(Float.class, float.class);
//        WRAPPERS_TO_PRIMITIVES.put(Integer.class, int.class);
//        WRAPPERS_TO_PRIMITIVES.put(Long.class, long.class);
//        WRAPPERS_TO_PRIMITIVES.put(Short.class, short.class);
//        WRAPPERS_TO_PRIMITIVES.put(Void.class, void.class);
//    }
//
//    /**
//     * Creates new instance from JPA entity class.
//     *
//     * @param em
//     * @param clazz must be JPA Entity annotated
//     */
//    public JpaQueryBuilder(EntityManager em, Class<T> clazz) {
//        this.clazz = clazz;
//        this.criteriaBuilder = em.getCriteriaBuilder();
//        //this.query = cb.createQuery(clazz);
//        //this.root = query.from(clazz);
//        this.options = EnumSet.noneOf(Logical.class);
//        this.predicateBuilder = new JpaPredicateBuilder(criteriaBuilder, options);
//        this.metamodel = em.getMetamodel();
//    }
//
//    /**
//     * Creates new instance from existing query
//     *
//     * @param em
//     * @param query
//     */
//    public JpaQueryBuilder(EntityManager em, CriteriaQuery<T> query) {
//        this.criteriaBuilder = em.getCriteriaBuilder();
//        this.clazz = query.getResultType();
//        //this.query = query;
//        this.options = EnumSet.noneOf(Logical.class);
//        this.predicateBuilder = new JpaPredicateBuilder(criteriaBuilder, options);
//        this.metamodel = em.getMetamodel();
//    }
//
//    /**
//     * Checks if filter fieldname is valid for given query. Throws verbose
//     * IllegalArgumentException if not. If checkValue = true, checks if filter
//     * value is of type of the field or can be converted. This is to avoid
//     * unnecessarry joins if filter is invalid
//     *
//     * @param filter
//     * @param checkValue if false than value is not checked
//     * @return true if filter is valid
//     * @throws IllegalArgumentException
//     */
//    private boolean checkFilterValid(PredicateFilter filter, boolean checkValue) {
//        Class<?> fieldType = getJavaType(filter.getField());
//        // arrays
//        if (filter.getValue().getClass().isArray()) {
//            Object[] arr = (Object[]) filter.getValue();
//            if (arr.length == 0) {
//                return false;
//            } else {
//                return !checkValue || ((Object[]) filter.getValue())[0].getClass().equals(fieldType);
//            }
//        }
//        if (fieldType.isPrimitive()) {
//            return !checkValue || PRIMITIVES_TO_WRAPPERS.get(fieldType).isInstance(filter.getValue());
//        } else {
//            return !checkValue || fieldType.isInstance(filter.getValue());
//        }
//    }
//
//    /**
//     * This clumsy code is just to get the class of plural attribute mapping
//     *
//     * @param et
//     * @param fieldName
//     * @return
//     */
//    private Class<?> getPluralJavaType(EntityType<?> et, String fieldName) {
//        for (PluralAttribute<?,?,?> pa : et.getPluralAttributes()) {
//            if (pa.getName().equals(fieldName)) {
//                switch (pa.getCollectionType()) {
//                    case COLLECTION:
//                        return et.getCollection(fieldName).getElementType().getJavaType();
//                    case LIST:
//                        return et.getList(fieldName).getElementType().getJavaType();
//                    case SET:
//                        return et.getSet(fieldName).getElementType().getJavaType();
//                    case MAP:
//                        throw new UnsupportedOperationException("Entity Map mapping unsupported for entity: " + et.getName() + " field name: " + fieldName);
//                }
//            }
//        }
//        throw new IllegalArgumentException("Field " + fieldName + " of entity " + et.getName() + " is not a plural attribute");
//    }
//
//    /**
//     * Returns Java type of the fieldName
//     *
//     * @param fieldName
//     * @return
//     * @throws IllegalArgumentException if fieldName isn't valid for given
//     *                                  entity
//     */
//    public Class<?> getJavaType(String fieldName) {
//
//        String[] compoundField = fieldName.split("\\.");
//        EntityType<?> et = metamodel.entity(clazz);
//
//        for (int i = 0; i < compoundField.length; i++) {
//            if (i < (compoundField.length - 1)) {
//                try {
//                    Attribute<?,?> att = et.getAttribute(compoundField[i]);
//                    if (att.isCollection()) {
//                        et = metamodel.entity(getPluralJavaType(et, compoundField[i]));
//                    } else {
//                        et = metamodel.entity(et.getAttribute(compoundField[i]).getJavaType());
//                    }
//                } catch (IllegalArgumentException | IllegalStateException e) {
//                    throw new IllegalArgumentException(
//                            "Illegal field name " + fieldName + " (" + compoundField[i] + ") for root type " + clazz
//                    );
//                }
//            } else {
//                try {
//                    return et.getAttribute(compoundField[i]).getJavaType();
//                } catch (IllegalArgumentException | IllegalStateException e) {
//                    throw new IllegalArgumentException(
//                            "Illegal field name " + fieldName + " (" + compoundField[i] + ") for root type " + clazz
//                    );
//                }
//            }
//        }
//        return null; // should never be reached
//    }
//
//    /**
//     * Adds filters to the query. Preserves existing filters.
//     *
//     * @param conditions
//     * @return this, itself for chain calls
//     */
//    public JpaQueryBuilder<T> addFilters(List<PredicateFilter> conditions) {
//        if (conditions != null) {
//            for (PredicateFilter f : conditions) {
//                if (checkFilterValid(f, true)) {
//                    filters.add(f);
//                } else {
//                    logger.error("Could not apply filter for field: " + f.getField() + " value: " + f.getValue() + " of type: " + f.getValue().getClass());
//                }
//            }
//        }
//
//        // keep it sorted to avoid inner/outer joins messup
//        Collections.sort(filters);
//
//        return this;
//    }
//
//    /**
//     * Adds filter to the query. Preserves existing filters.
//     *
//     * @param conditions
//     * @return this, itself for chain calls
//     */
//    public JpaQueryBuilder<T> addFilter(PredicateFilter filter) {
//        if (filter != null) {
//            if (checkFilterValid(filter, true)) {
//                filters.add(filter);
//            } else {
//                logger.error("Could not apply filter for field: " + filter.getField() + " value: " + filter.getValue() + " of type: " + filter.getValue().getClass());
//            }
//        }
//        
//        // keep it sorted to avoid inner/outer joins messup
//        Collections.sort(filters);
//
//        return this;
//    }
//    
//    /**
//     * @param from
//     * @param query
//     */
//    private void applyFilters(Root<T> from, CriteriaQuery<?> query) {
//
//        List<Predicate> predicates = new ArrayList<>();
//
//        for (PredicateFilter filter : filters) {
//            Path<?> path;
//            if (filter.getCriterias().contains(PredicateFilter.Criteria.IS_NULL)) {
//                path = getCompoundJoinedPath(from, filter.getField(), true);
//            } else {
//                path = getCompoundJoinedPath(from, filter.getField(), false);
//            }
//
//            Predicate p = predicateBuilder.getPredicate(from, path, filter);
//            if (p != null) {
//                predicates.add(p);
//            }
//        }
//        // this does not work for Hibernate!!!
//        if (query.getRestriction() != null) {
//            predicates.add(query.getRestriction());
//        }
//        if (options.contains(Logical.OR)) {
//            query.where(criteriaBuilder.or(predicates.toArray(new Predicate[0])));
//        } else {
//            query.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
//        }
//
//    }
//
//    private void applyOrders(Root<T> from, CriteriaQuery<T> query) {
//        List<Order> orderList = new ArrayList<>();
//
//        for (Map.Entry<String, Boolean> me : orders.entrySet()) {
//            Path<?> path = getCompoundJoinedPath(from, me.getKey(), true);
//            if (me.getValue() == null || me.getValue().equals(true)) {
//                orderList.add(criteriaBuilder.asc(path));
//            } else {
//                orderList.add(criteriaBuilder.desc(path));
//            }
//        }
//        query.orderBy(orderList);
//    }
//
//    /**
//     * Adds order by expressions to the tail of already existing orders of query
//     *
//     * @param orders
//     * @return
//     */
//    public JpaQueryBuilder<T> addOrders(Map<String, Boolean> orders) {
//
//        for (Map.Entry<String, Boolean> me : orders.entrySet()) {
//            checkFilterValid(new PredicateFilter(me.getKey(), "", EnumSet.noneOf(PredicateFilter.Criteria.class)), false);
//            this.orders.put(me.getKey(), me.getValue());
//        }
//        return this;
//    }
//
//    /**
//     * Adds order by expressions to the tail of already existing orders of query
//     *
//     * @param orders
//     * @return
//     */
//    public JpaQueryBuilder<T> addOrder(String field, Boolean direction) {
//
//            checkFilterValid(new PredicateFilter(field, "", EnumSet.noneOf(PredicateFilter.Criteria.class)), false);
//            this.orders.put(field, direction);
//
//         return this;
//    }
//    
//    /**
//     * @param fieldName
//     * @return Path of compound field to the primitive type
//     */
//    private Path<?> getCompoundJoinedPath(Root<T> rootPath, String fieldName, boolean outer) {
//        String[] compoundField = fieldName.split("\\.");
//
//        Join<?,?> join;
//
//        if (compoundField.length == 1) {
//            return rootPath.get(compoundField[0]);
//        } else {
//            join = reuseJoin(rootPath, compoundField[0], outer);
//        }
//
//        for (int i = 1; i < compoundField.length; i++) {
//            if (i < (compoundField.length - 1)) {
//                join = reuseJoin(join, compoundField[i], outer);
//            } else {
//                return join.get(compoundField[i]);
//            }
//        }
//
//        return null;
//    }
//
//    // trying to find already existing joins to reuse
//    private Join<?,?> reuseJoin(From<?, ?> path, String fieldName, boolean outer) {
//        for (Join<?,?> join : path.getJoins()) {
//            if (join.getAttribute().getName().equals(fieldName)) {
//                if ((join.getJoinType() == JoinType.LEFT) == outer) {
//                    logger.debug("Reusing existing join for field " + fieldName);
//                    return join;
//                }
//            }
//        }
//        return outer ? path.join(fieldName, JoinType.LEFT) : path.join(fieldName);
//    }
//
//
//    /**
//     * Get sorting field
//     *
//     * @param clazz
//     * @return
//     * @throws IllegalArgumentException
//     * @throws IllegalAccessException
//     */
//    private Field getSortAnnotation(Class<?> clazz) throws IllegalArgumentException, IllegalAccessException {
//        for (Field f : clazz.getDeclaredFields()) {
//            if (f.getAnnotation(GraphQLDefaultOrderBy.class) != null) {
//                return f;
//            }
//        }
//        //if not found, search in superclass. todo recursive search
//        for (Field f : clazz.getSuperclass().getDeclaredFields()) {
//            if (f.getAnnotation(GraphQLDefaultOrderBy.class) != null) {
//                return f;
//            }
//        }
//        return null;
//    }
//
//
//    /**
//     * Resulting query with filters and orders, if orders are empty, than makes
//     * default ascending ordering by root id to prevent paging confuses
//     *
//     * @return
//     */
//    public CriteriaQuery<T> getQuery() {
//        CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
//        Root<T> from = query.from(clazz);
//        applyFilters(from, query);
//        applyOrders(from, query);
//
//        // add default ordering
//        if (query.getOrderList() == null || query.getOrderList().isEmpty()) {
//            EntityType<T> entityType = from.getModel();
//            try {
//                Field sortField = getSortAnnotation(entityType.getBindableJavaType());
//                if (sortField == null)
//                    query.orderBy(criteriaBuilder.asc(from.get(entityType.getId(entityType.getIdType().getJavaType()).getName())));
//                else {
//                    GraphQLDefaultOrderBy order = sortField.getAnnotation(GraphQLDefaultOrderBy.class);
//                    if (order.asc()) {
//                        query.orderBy(criteriaBuilder.asc(from.get(sortField.getName())));
//                    } else {
//                        query.orderBy(criteriaBuilder.desc(from.get(sortField.getName())));
//                    }
//
//                }
//            } catch (Exception ex) {
//                logger.warn("In" + this.getClass().getName(), ex);
//            }
//        }
//        return query;
//    }
//
//    /**
//     * @return
//     */
//    public CriteriaQuery<Long> getCountQuery() {
//        CriteriaQuery<Long> q = criteriaBuilder.createQuery(Long.class);
//        Root<T> root = q.from(clazz);
//        q.select(criteriaBuilder.count(root));
//        applyFilters(root, q);
//        return q;
//    }
//
//    public EnumSet<Logical> getOptions() {
//        return options;
//    }
//
//}