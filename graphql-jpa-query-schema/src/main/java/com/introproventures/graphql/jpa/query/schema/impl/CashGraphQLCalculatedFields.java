package com.introproventures.graphql.jpa.query.schema.impl;


import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CashGraphQLCalculatedFields {
    protected static Map<Class, Map<String, Optional<Transient>>> cashCalcFields = new ConcurrentHashMap<>();

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

        Optional<Transient> cf = getTransient(cls, field);
        addCashCalcFields(cls, field, cf);

        return cf.isPresent();
    }

    public static void addCashCalcFields(Class cls, String field, Optional<Transient> an) {
        if (!cashCalcFields.containsKey(cls)) {
            Map<String, Optional<Transient>> tpMap = new ConcurrentHashMap<>();
            cashCalcFields.put(cls, tpMap);
        }

        cashCalcFields.get(cls).put(field, an);
    }

    public static Optional<Transient> getTransient(Class cls, String field) {
        Optional<Transient> calcField = Arrays.stream(cls.getDeclaredFields())
                .filter(f -> f.getName().equals(field) && f.isAnnotationPresent(Transient.class))
                .map(f -> f.getAnnotation(Transient.class))
                .findFirst();

        if (!calcField.isPresent()) {
            calcField = getGraphQLCalcMethod(cls, field, "get");
        }

        if (!calcField.isPresent()) {
            calcField = getGraphQLCalcMethod(cls, field, "is");
        }

        return calcField;
    }

    public static Optional<Transient> getGraphQLCalcMethod(Class cls, String field, String prefix) {
        String methodName = prefix + field.substring(0,1).toUpperCase() + field.substring(1);

        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName) && m.isAnnotationPresent(Transient.class))
                .map(m -> m.getAnnotation(Transient.class))
                .findFirst()
                ;
    }
}
