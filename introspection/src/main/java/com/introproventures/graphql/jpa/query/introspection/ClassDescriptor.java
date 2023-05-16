package com.introproventures.graphql.jpa.query.introspection;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClassDescriptor {

    protected final Class<?> type;
    protected final boolean scanAccessible;
    protected final boolean scanStatics;
    protected final boolean extendedProperties;
    protected final boolean includeFieldsAsProperties;
    protected final String propertyFieldPrefix;
    protected final Class<?>[] interfaces;
    protected final Class<?>[] superclasses;
    protected int usageCount;

    private final boolean isArray;
    private final boolean isMap;
    private final boolean isList;
    private final boolean isSet;
    private final boolean isCollection;
    private final Fields fields;
    private final Methods methods;
    private final Properties properties;
    private final Constructors constructors;

    private final Annotations annotations;

    public ClassDescriptor(
        Class<?> type,
        boolean scanAccessible,
        boolean extendedProperties,
        boolean includeFieldsAsProperties,
        boolean scanStatics,
        String propertyFieldPrefix
    ) {
        this.type = type;
        this.scanAccessible = scanAccessible;
        this.extendedProperties = extendedProperties;
        this.includeFieldsAsProperties = includeFieldsAsProperties;
        this.propertyFieldPrefix = propertyFieldPrefix;
        this.scanStatics = scanStatics;

        isArray = type.isArray();
        isMap = Map.class.isAssignableFrom(type);
        isList = List.class.isAssignableFrom(type);
        isSet = Set.class.isAssignableFrom(type);
        isCollection = Collection.class.isAssignableFrom(type);

        interfaces = ClassUtil.getAllInterfacesAsArray(type);
        superclasses = ClassUtil.getAllSuperclassesAsArray(type);

        fields = new Fields(this);
        methods = new Methods(this);
        properties = new Properties(this);
        constructors = new Constructors(this);

        annotations = new Annotations(type);
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isScanAccessible() {
        return scanAccessible;
    }

    public boolean isExtendedProperties() {
        return extendedProperties;
    }

    public boolean isIncludeFieldsAsProperties() {
        return includeFieldsAsProperties;
    }

    public String getPropertyFieldPrefix() {
        return propertyFieldPrefix;
    }

    protected void increaseUsageCount() {
        usageCount++;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public boolean isArray() {
        return isArray;
    }

    public boolean isMap() {
        return isMap;
    }

    public boolean isList() {
        return isList;
    }

    public boolean isSet() {
        return isSet;
    }

    public boolean isCollection() {
        return isCollection;
    }

    protected Fields getFields() {
        return fields;
    }

    public FieldDescriptor getFieldDescriptor(String name, boolean declared) {
        FieldDescriptor fieldDescriptor = getFields().getFieldDescriptor(name);

        if (fieldDescriptor != null) {
            if (!fieldDescriptor.matchDeclared(declared)) {
                return null;
            }
        }

        return fieldDescriptor;
    }

    public FieldDescriptor[] getAllFieldDescriptors() {
        return getFields().getAllFieldDescriptors();
    }

    protected Methods getMethods() {
        return methods;
    }

    public MethodDescriptor getMethodDescriptor(String name, boolean declared) {
        MethodDescriptor methodDescriptor = getMethods().getMethodDescriptor(name);

        if ((methodDescriptor != null) && methodDescriptor.matchDeclared(declared)) {
            return methodDescriptor;
        }

        return methodDescriptor;
    }

    public MethodDescriptor getMethodDescriptor(String name, Class<?>[] params, boolean declared) {
        MethodDescriptor methodDescriptor = getMethods().getMethodDescriptor(name, params);

        if ((methodDescriptor != null) && methodDescriptor.matchDeclared(declared)) {
            return methodDescriptor;
        }

        return null;
    }

    public MethodDescriptor[] getAllMethodDescriptors(String name) {
        return getMethods().getAllMethodDescriptors(name);
    }

    public MethodDescriptor[] getAllMethodDescriptors() {
        return getMethods().getAllMethodDescriptors();
    }

    // ----------------------------------------------------------------
    // properties

    protected Properties getProperties() {
        return properties;
    }

    public PropertyDescriptor getPropertyDescriptor(String name, boolean declared) {
        PropertyDescriptor propertyDescriptor = getProperties().getPropertyDescriptor(name);

        if ((propertyDescriptor != null) && propertyDescriptor.matchDeclared(declared)) {
            return propertyDescriptor;
        }

        return null;
    }

    public PropertyDescriptor[] getAllPropertyDescriptors() {
        return getProperties().getAllPropertyDescriptors();
    }

    // ----------------------------------------------------------------
    // constructors

    protected Constructors getConstructors() {
        return constructors;
    }

    public ConstructorDescriptor getDefaultCtorDescriptor(boolean declared) {
        ConstructorDescriptor defaultConstructor = getConstructors().getDefaultCtor();

        if ((defaultConstructor != null) && defaultConstructor.matchDeclared(declared)) {
            return defaultConstructor;
        }
        return null;
    }

    public ConstructorDescriptor getConstructorDescriptor(Class<?>[] args, boolean declared) {
        ConstructorDescriptor constructorDescriptor = getConstructors().getCtorDescriptor(args);

        if ((constructorDescriptor != null) && constructorDescriptor.matchDeclared(declared)) {
            return constructorDescriptor;
        }
        return null;
    }

    public ConstructorDescriptor[] getAllConstructorDescriptors() {
        return getConstructors().getAllCtorDescriptors();
    }

    // ----------------------------------------------------------------
    // annotations

    protected Annotations getAnnotations() {
        return annotations;
    }

    public AnnotationDescriptor getAnnotationDescriptor(Class<? extends Annotation> clazz) {
        return annotations.getAnnotationDescriptor(clazz);
    }

    public AnnotationDescriptor[] getAllAnnotationDescriptors() {
        return annotations.getAllAnnotationDescriptors();
    }

    public Class<?>[] getAllInterfaces() {
        return interfaces;
    }

    public Class<?>[] getAllSuperclasses() {
        return superclasses;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("ClassDescriptor [type=")
            .append(type)
            .append(", scanAccessible=")
            .append(scanAccessible)
            .append(", extendedProperties=")
            .append(extendedProperties)
            .append(", includeFieldsAsProperties=")
            .append(includeFieldsAsProperties)
            .append(", propertyFieldPrefix=")
            .append(propertyFieldPrefix)
            .append(", usageCount=")
            .append(usageCount)
            .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ClassDescriptor other = (ClassDescriptor) obj;
        return Objects.equals(type, other.type);
    }

    public boolean isScanStatics() {
        return scanStatics;
    }
}
