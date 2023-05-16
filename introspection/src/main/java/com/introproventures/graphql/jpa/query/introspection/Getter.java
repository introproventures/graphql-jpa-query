package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.InvocationTargetException;

public interface Getter {
    Object invokeGetter(Object target) throws InvocationTargetException, IllegalAccessException;

    Class<?> getGetterRawType();

    Class<?> getGetterRawComponentType();

    Class<?> getGetterRawKeyComponentType();
}
