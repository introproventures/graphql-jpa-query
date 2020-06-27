package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class MethodDescriptor extends Descriptor implements Getter, Setter {

    protected final Method method;
    protected final Type returnType;
    protected final Class<?> rawReturnType;
    protected final Class<?> rawReturnComponentType;
    protected final Class<?> rawReturnKeyComponentType;
    protected final Class<?>[] rawParameterTypes;
    protected final Class<?>[] rawParameterComponentTypes;

    public MethodDescriptor(ClassDescriptor classDescriptor, Method method) {
        super(classDescriptor, ReflectionUtil.isPublic(method));
        this.method = method;
        this.returnType = method.getGenericReturnType();
        this.rawReturnType = ReflectionUtil.getRawType(returnType, classDescriptor.getType());

        Class<?>[] componentTypes = ReflectionUtil.getComponentTypes(returnType, classDescriptor.getType());
        if (componentTypes != null) {
            this.rawReturnComponentType = componentTypes[componentTypes.length - 1];
            this.rawReturnKeyComponentType = componentTypes[0];
        } else {
            this.rawReturnComponentType = null;
            this.rawReturnKeyComponentType = null;
        }

        annotations = new Annotations(method);

        ReflectionUtil.forceAccess(method);

        Type[] params = method.getGenericParameterTypes();
        Type[] genericParams = method.getGenericParameterTypes();

        rawParameterTypes = new Class[params.length];
        rawParameterComponentTypes = genericParams.length == 0 ? null : new Class[params.length];

        for (int i = 0; i < params.length; i++) {
            Type type = params[i];
            rawParameterTypes[i] = ReflectionUtil.getRawType(type, classDescriptor.getType());
            if (rawParameterComponentTypes != null) {
                rawParameterComponentTypes[i] =
                        ReflectionUtil.getComponentType(genericParams[i], classDescriptor.getType());
            }
        }
    }

    @Override
    public String getName() {
        return method.getName();
    }

    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getRawReturnType() {
        return rawReturnType;
    }

    public Class<?> getRawReturnComponentType() {
        return rawReturnComponentType;
    }

    public Class<?> getRawReturnKeyComponentType() {
        return rawReturnKeyComponentType;
    }

    public Class<?>[] resolveRawReturnComponentTypes() {
        return ReflectionUtil.getComponentTypes(returnType, classDescriptor.getType());
    }

    public Class<?>[] getRawParameterTypes() {
        return rawParameterTypes;
    }

    public Class<?>[] getRawParameterComponentTypes() {
        return rawParameterComponentTypes;
    }

    @Override
    public Object invokeGetter(Object target) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target);
    }

    @Override
    public Class<?> getGetterRawType() {
        return getRawReturnType();
    }

    @Override
    public Class<?> getGetterRawComponentType() {
        return getRawReturnComponentType();
    }

    @Override
    public Class<?> getGetterRawKeyComponentType() {
        return getRawReturnKeyComponentType();
    }

    @Override
    public void invokeSetter(Object target, Object argument) throws IllegalAccessException, InvocationTargetException {
        method.invoke(target, argument);
    }

    @Override
    public Class<?> getSetterRawType() {
        return getRawParameterTypes()[0];
    }

    @Override
    public Class<?> getSetterRawComponentType() {
        Class<?>[] ts = getRawParameterComponentTypes();
        if (ts == null) {
            return null;
        }
        return ts[0];
    }

    @Override
    public String toString() {
        return classDescriptor.getType().getSimpleName() + '#' + method.getName() + "()";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(rawParameterTypes);
        result = prime * result + Objects.hash(method, returnType);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodDescriptor other = (MethodDescriptor) obj;
        return Objects.equals(method, other.method) && Arrays.equals(rawParameterTypes,
                                                                     other.rawParameterTypes) && Objects.equals(returnType,
                                                                                                                other.returnType);
    }

}