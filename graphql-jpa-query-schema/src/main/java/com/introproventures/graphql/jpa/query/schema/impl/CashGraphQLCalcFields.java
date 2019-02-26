package com.introproventures.graphql.jpa.query.schema.impl;

import com.introproventures.graphql.jpa.query.annotation.GraphQLCalcField;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CashGraphQLCalcFields {
    protected static Map<Class, Map<String, Optional<GraphQLCalcField>>> cashCalcFields = new ConcurrentHashMap<>();

    public static void clearCashCalcFields() {
        cashCalcFields.values().stream().forEach(v -> v.clear());
        cashCalcFields.clear();
    }

    public static boolean isCalcField(Class cls, String field) {
        if (cashCalcFields.containsKey(cls)) {
            if (cashCalcFields.get(cls).containsKey(field)) {
                return cashCalcFields.get(cls).get(field).isPresent();
            }
        }

        Optional<GraphQLCalcField> cf = getGraphQLCalcField(cls, field);
        addCashCalcFields(cls, field, cf);

        return cf.isPresent();
    }

    public static void addCashCalcFields(Class cls, String field, Optional<GraphQLCalcField> an) {
        if (!cashCalcFields.containsKey(cls)) {
            Map<String, Optional<GraphQLCalcField>> tpMap = new ConcurrentHashMap<>();
            cashCalcFields.put(cls, tpMap);
        }

        cashCalcFields.get(cls).put(field, an);
    }

    public static Optional<GraphQLCalcField> getGraphQLCalcField(Class cls, String field) {
        Optional<GraphQLCalcField> calcField = Arrays.stream(cls.getDeclaredFields())
                .filter(f -> f.getName().equals(field) && f.isAnnotationPresent(GraphQLCalcField.class))
                .map(f -> f.getAnnotation(GraphQLCalcField.class))
                .findFirst();

        if (!calcField.isPresent()) {
            calcField = getGraphQLCalcMethod(cls, field, "get");
        }

        if (!calcField.isPresent()) {
            calcField = getGraphQLCalcMethod(cls, field, "is");
        }

        if (!calcField.isPresent()) {
            calcField = getGraphQLCalcMethod(cls, field, "");
        }

        return calcField;
    }

    public static Optional<GraphQLCalcField> getGraphQLCalcMethod(Class cls, String field, String prefix) {
        String methodName = prefix + field.substring(0,1).toUpperCase() + field.substring(1);

        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName) && m.isAnnotationPresent(GraphQLCalcField.class))
                .map(m -> m.getAnnotation(GraphQLCalcField.class))
                .findFirst()
                ;
    }
}
