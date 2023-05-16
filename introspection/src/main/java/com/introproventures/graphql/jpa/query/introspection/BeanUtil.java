package com.introproventures.graphql.jpa.query.introspection;

import java.beans.Introspector;
import java.lang.reflect.Method;

public abstract class BeanUtil {

    public static final String METHOD_GET_PREFIX = "get";
    public static final String METHOD_IS_PREFIX = "is";
    public static final String METHOD_SET_PREFIX = "set";

    public static String getBeanGetterName(Method method) {
        if (method == null) {
            return null;
        }

        int prefixLength = getBeanGetterPrefixLength(method);
        if (prefixLength == 0) {
            return null;
        }

        String methodName = method.getName().substring(prefixLength);
        return Introspector.decapitalize(methodName);
    }

    private static int getBeanGetterPrefixLength(Method method) {
        if (isObjectMethod(method)) {
            return 0;
        }
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (methodName.startsWith(METHOD_GET_PREFIX) && ((returnType != null) && (paramTypes.length == 0))) {
            return 3;
        }

        if (methodName.startsWith(METHOD_IS_PREFIX) && ((returnType != null) && (paramTypes.length == 0))) {
            return 2;
        }

        return 0;
    }

    public static boolean isBeanProperty(Method method) {
        if (method == null || isObjectMethod(method)) {
            return false;
        }
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (methodName.startsWith(METHOD_GET_PREFIX) && ((returnType != null) && (paramTypes.length == 0))) {
            return true;
        }
        if (methodName.startsWith(METHOD_IS_PREFIX) && ((returnType != null) && (paramTypes.length == 0))) {
            return true;
        }
        if (methodName.startsWith(METHOD_SET_PREFIX) && paramTypes.length == 1) {
            return true;
        }

        return false;
    }

    public static boolean isBeanSetter(Method method) {
        return getBeanSetterPrefixLength(method) != 0;
    }

    private static int getBeanSetterPrefixLength(Method method) {
        if (isObjectMethod(method)) {
            return 0;
        }
        String methodName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        if (methodName.startsWith(METHOD_SET_PREFIX)) {
            if (paramTypes.length == 1) {
                return 3;
            }
        }
        return 0;
    }

    public static String getBeanSetterName(Method method) {
        if (method == null) {
            return null;
        }

        int prefixLength = getBeanSetterPrefixLength(method);
        if (prefixLength == 0) {
            return null;
        }

        String methodName = method.getName().substring(prefixLength);
        return Introspector.decapitalize(methodName);
    }

    public static boolean isBeanGetter(Method method) {
        if (method == null) {
            return false;
        }

        return getBeanGetterPrefixLength(method) != 0;
    }

    private static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }
}
