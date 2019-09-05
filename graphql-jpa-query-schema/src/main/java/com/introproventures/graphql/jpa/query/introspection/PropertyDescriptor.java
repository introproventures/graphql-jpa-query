package com.introproventures.graphql.jpa.query.introspection;

import java.lang.reflect.Field;
import java.util.Arrays;

public class PropertyDescriptor extends Descriptor {

    protected final String name;
    protected final MethodDescriptor readMethodDescriptor;
    protected final MethodDescriptor writeMethodDescriptor;
    protected final FieldDescriptor fieldDescriptor;

    protected Class<?> type;
    protected Getter[] getters;
    protected Setter[] setters;

    public PropertyDescriptor(ClassDescriptor classDescriptor, String propertyName, FieldDescriptor fieldDescriptor) {
        super(classDescriptor, false);
        this.name = propertyName;
        this.readMethodDescriptor = null;
        this.writeMethodDescriptor = null;
        this.fieldDescriptor = fieldDescriptor;
    }

    public PropertyDescriptor(ClassDescriptor classDescriptor, String propertyName, MethodDescriptor readMethod,
            MethodDescriptor writeMethod) {
        super(classDescriptor, ((readMethod == null) || readMethod.isPublic())
                & (writeMethod == null || writeMethod.isPublic()));
        this.name = propertyName;
        this.readMethodDescriptor = readMethod;
        this.writeMethodDescriptor = writeMethod;

        if (classDescriptor.isExtendedProperties()) {
            this.fieldDescriptor = findField(propertyName);
        } else {
            this.fieldDescriptor = null;
        }
    }

    protected FieldDescriptor findField(String fieldName) {
        String prefix = classDescriptor.getPropertyFieldPrefix();

        if (prefix != null) {
            fieldName = prefix + fieldName;
        }

        return classDescriptor.getFieldDescriptor(fieldName, true);
    }

    @Override
    public String getName() {
        return name;
    }

    public MethodDescriptor getReadMethodDescriptor() {
        return readMethodDescriptor;
    }

    public MethodDescriptor getWriteMethodDescriptor() {
        return writeMethodDescriptor;
    }

    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }

    public boolean isFieldOnlyDescriptor() {
        return (readMethodDescriptor == null) && (writeMethodDescriptor == null);
    }

    public Class<?> getType() {
        if (type == null) {
            if (readMethodDescriptor != null) {
                type = readMethodDescriptor.getMethod().getReturnType();
            } else if (writeMethodDescriptor != null) {
                type = writeMethodDescriptor.getMethod().getParameterTypes()[0];
            } else if (fieldDescriptor != null) {
                type = fieldDescriptor.getField().getType();
            }
        }

        return type;
    }

    public Getter getGetter(boolean declared) {
        if (getters == null) {
            getters = new Getter[] { createGetter(false), createGetter(true), };
        }

        return getters[declared ? 1 : 0];
    }

    protected Getter createGetter(boolean declared) {
        if (readMethodDescriptor != null) {
            if (readMethodDescriptor.matchDeclared(declared)) {
                return readMethodDescriptor;
            }
        }
        if (fieldDescriptor != null) {
            if (fieldDescriptor.matchDeclared(declared)) {
                return fieldDescriptor;
            }
        }

        return null;
    }

    public Setter getSetter(boolean declared) {
        if (setters == null) {
            setters = new Setter[] { createSetter(false), createSetter(true), };
        }

        return setters[declared ? 1 : 0];
    }

    protected Setter createSetter(boolean declared) {
        if (writeMethodDescriptor != null) {
            if (writeMethodDescriptor.matchDeclared(declared)) {
                return writeMethodDescriptor;
            }
        }
        if (fieldDescriptor != null) {
            if (fieldDescriptor.matchDeclared(declared)) {
                return fieldDescriptor;
            }
        }

        return null;
    }

    public Class<?> resolveKeyType(boolean declared) {
        Class<?> keyType = null;

        Getter getter = getGetter(declared);

        if (getter != null) {
            keyType = getter.getGetterRawKeyComponentType();
        }

        if (keyType == null) {
            FieldDescriptor fieldDescriptor = getFieldDescriptor();

            if (fieldDescriptor != null) {
                keyType = fieldDescriptor.getRawKeyComponentType();
            }
        }

        return keyType;
    }

    public Class<?> resolveComponentType(boolean declared) {
        Class<?> componentType = null;

        Getter getter = getGetter(declared);

        if (getter != null) {
            componentType = getter.getGetterRawComponentType();
        }

        if (componentType == null) {
            FieldDescriptor fieldDescriptor = getFieldDescriptor();

            if (fieldDescriptor != null) {
                componentType = fieldDescriptor.getRawComponentType(); 
            }
        }

        return componentType;
    }

    // add
    public Field getField() {
        Class<?> clazz = this.getClassDescriptor().getType();

        return ReflectionUtil.getField(clazz, this.getName()); 
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PropertyDescriptor [name=")
               .append(name)
               .append(", readMethodDescriptor=")
               .append(readMethodDescriptor)
               .append(", writeMethodDescriptor=")
               .append(writeMethodDescriptor)
               .append(", fieldDescriptor=")
               .append(fieldDescriptor)
               .append(", type=")
               .append(type)
               .append(", getters=")
               .append(Arrays.toString(getters))
               .append(", setters=")
               .append(Arrays.toString(setters))
               .append(", classDescriptor=")
               .append(classDescriptor)
               .append(", isPublic=")
               .append(isPublic)
               .append(", annotations=")
               .append(annotations)
               .append("]");
        return builder.toString();
    }
}