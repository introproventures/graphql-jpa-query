package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class FieldDescriptor extends Descriptor implements Getter, Setter {

    protected final Field field;
    protected final Type type;
    protected final Class<?> rawType;
    protected final Class<?> rawComponentType;
    protected final Class<?> rawKeyComponentType;

    public FieldDescriptor(ClassDescriptor classDescriptor, Field field) {
        super(classDescriptor, ReflectionUtil.isPublic(field));
        this.field = field;
        this.type = field.getGenericType();
        this.rawType = ReflectionUtil.getRawType(type, classDescriptor.getType());

        Class<?>[] componentTypes = ReflectionUtil.getComponentTypes(type, classDescriptor.getType());
        if (componentTypes != null) {
            this.rawComponentType = componentTypes[componentTypes.length - 1];
            this.rawKeyComponentType = componentTypes[0];
        } else {
            this.rawComponentType = null;
            this.rawKeyComponentType = null;
        }

        annotations = new Annotations(field);

        ReflectionUtil.forceAccess(field);
    }

    @Override
    public String getName() {
        return field.getName();
    }

    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    public Field getField() {
        return field;
    }

    public Class<?> getRawType() {
        return rawType;
    }

    public Class<?> getRawComponentType() {
        return rawComponentType;
    }

    public Class<?> getRawKeyComponentType() {
        return rawKeyComponentType;
    }

    public Class<?>[] resolveRawComponentTypes() {
        return ReflectionUtil.getComponentTypes(type, classDescriptor.getType());
    }

    public Object invokeGetter(Object target) throws InvocationTargetException, IllegalAccessException {
        return field.get(target);
    }

    public Class<?> getGetterRawType() {
        return getRawType();
    }

    public Class<?> getGetterRawComponentType() {
        return getRawComponentType();
    }

    public Class<?> getGetterRawKeyComponentType() {
        return getRawKeyComponentType();
    }

    public void invokeSetter(Object target, Object argument) throws IllegalAccessException {
        field.set(target, argument);
    }

    public Class<?> getSetterRawType() {
        return getRawType();
    }

    public Class<?> getSetterRawComponentType() {
        return getRawComponentType();
    }

    @Override
    public String toString() {
        return classDescriptor.getType().getSimpleName() + '#' + field.getName();
    }

}