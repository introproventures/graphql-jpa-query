package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.InvocationTargetException;

public interface Setter {

    void invokeSetter(Object target, Object argument) throws IllegalAccessException, InvocationTargetException;

    Class<?> getSetterRawType();

    Class<?> getSetterRawComponentType();
}
