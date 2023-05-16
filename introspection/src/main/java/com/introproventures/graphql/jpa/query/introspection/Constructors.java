package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

public class Constructors {

    protected final ClassDescriptor classDescriptor;
    protected final ConstructorDescriptor[] allConstructors;
    protected ConstructorDescriptor defaultConstructor;

    public Constructors(ClassDescriptor classDescriptor) {
        this.classDescriptor = classDescriptor;
        this.allConstructors = inspectConstructors();
    }

    protected ConstructorDescriptor[] inspectConstructors() {
        Class<?> type = classDescriptor.getType();
        Constructor<?>[] ctors = type.getDeclaredConstructors();

        ConstructorDescriptor[] allConstructors = new ConstructorDescriptor[ctors.length];

        for (int i = 0; i < ctors.length; i++) {
            Constructor<?> ctor = ctors[i];

            ConstructorDescriptor ctorDescriptor = createCtorDescriptor(ctor);
            allConstructors[i] = ctorDescriptor;

            if (ctorDescriptor.isDefault()) {
                defaultConstructor = ctorDescriptor;
            }
        }

        return allConstructors;
    }

    protected ConstructorDescriptor createCtorDescriptor(Constructor<?> constructor) {
        return new ConstructorDescriptor(classDescriptor, constructor);
    }

    public ConstructorDescriptor getDefaultCtor() {
        return defaultConstructor;
    }

    public ConstructorDescriptor getCtorDescriptor(Class<?>... args) {
        ctors:for (ConstructorDescriptor ctorDescriptor : allConstructors) {
            Class<?>[] arg = ctorDescriptor.getParameters();

            if (arg.length != args.length) {
                continue;
            }

            for (int j = 0; j < arg.length; j++) {
                if (arg[j] != args[j]) {
                    continue ctors;
                }
            }

            return ctorDescriptor;
        }
        return null;
    }

    ConstructorDescriptor[] getAllCtorDescriptors() {
        return allConstructors;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("Constructors [classDescriptor=")
            .append(classDescriptor)
            .append(", allConstructors=")
            .append(Arrays.toString(allConstructors))
            .append(", defaultConstructor=")
            .append(defaultConstructor)
            .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(allConstructors);
        result = prime * result + Objects.hash(classDescriptor, defaultConstructor);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Constructors other = (Constructors) obj;
        return (
            Arrays.equals(allConstructors, other.allConstructors) &&
            Objects.equals(classDescriptor, other.classDescriptor) &&
            Objects.equals(defaultConstructor, other.defaultConstructor)
        );
    }
}
