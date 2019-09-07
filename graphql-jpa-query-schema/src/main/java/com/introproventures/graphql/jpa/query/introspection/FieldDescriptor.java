package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Objects;

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

    @Override
    public Object invokeGetter(Object target) throws InvocationTargetException, IllegalAccessException {
        return field.get(target);
    }

    @Override
    public Class<?> getGetterRawType() {
        return getRawType();
    }

    @Override
    public Class<?> getGetterRawComponentType() {
        return getRawComponentType();
    }

    @Override
    public Class<?> getGetterRawKeyComponentType() {
        return getRawKeyComponentType();
    }

    @Override
    public void invokeSetter(Object target, Object argument) throws IllegalAccessException {
        field.set(target, argument);
    }

    @Override
    public Class<?> getSetterRawType() {
        return getRawType();
    }

    @Override
    public Class<?> getSetterRawComponentType() {
        return getRawComponentType();
    }

    @Override
    public String toString() {
        return classDescriptor.getType().getSimpleName() + '#' + field.getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(field, type);
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
        FieldDescriptor other = (FieldDescriptor) obj;
        return Objects.equals(field, other.field) && Objects.equals(type, other.type);
    }

}