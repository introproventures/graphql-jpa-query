package com.introproventures.graphql.jpa.query.introspection;


import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

public class ConstructorDescriptor extends Descriptor {

    protected final Constructor<?> constructor;
    protected final Class<?>[] parameters;

    public ConstructorDescriptor(ClassDescriptor classDescriptor, Constructor<?> constructor) {
        super(classDescriptor, ReflectionUtil.isPublic(constructor));
        this.constructor = constructor;
        this.parameters = constructor.getParameterTypes();

        annotations = new Annotations(constructor);

        ReflectionUtil.forceAccess(constructor);
    }

    @Override
    public String getName() {
        return constructor.getName();
    }

    public Class<?> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }

    public boolean isDefault() {
        return parameters.length == 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConstructorDescriptor [constructor=")
               .append(constructor)
               .append(", parameters=")
               .append(Arrays.toString(parameters))
               .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(parameters);
        result = prime * result + Objects.hash(constructor);
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
        ConstructorDescriptor other = (ConstructorDescriptor) obj;
        return Objects.equals(constructor, other.constructor) 
                && Arrays.equals(parameters, other.parameters);
    }

}