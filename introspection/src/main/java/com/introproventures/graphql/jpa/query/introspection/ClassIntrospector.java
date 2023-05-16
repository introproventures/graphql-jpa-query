package com.introproventures.graphql.jpa.query.introspection;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassIntrospector {

    protected final Map<Class<?>, ClassDescriptor> cache = new LinkedHashMap<>();
    protected final boolean scanAccessible;
    protected final boolean enhancedProperties;
    protected final boolean includeFieldsAsProperties;
    protected final boolean scanStatics;
    protected final String propertyFieldPrefix;

    private ClassIntrospector(Builder builder) {
        this.scanAccessible = builder.scanAccessible;
        this.enhancedProperties = builder.enhancedProperties;
        this.includeFieldsAsProperties = builder.includeFieldsAsProperties;
        this.propertyFieldPrefix = builder.propertyFieldPrefix;
        this.scanStatics = builder.scanStatics;
    }

    public ClassIntrospector() {
        this(true, true, true, true, null);
    }

    public ClassIntrospector(
        boolean scanAccessible,
        boolean enhancedProperties,
        boolean includeFieldsAsProperties,
        boolean scanStatics,
        String propertyFieldPrefix
    ) {
        this.scanAccessible = scanAccessible;
        this.enhancedProperties = enhancedProperties;
        this.includeFieldsAsProperties = includeFieldsAsProperties;
        this.propertyFieldPrefix = propertyFieldPrefix;
        this.scanStatics = scanStatics;
    }

    public ClassDescriptor introspect(Class<?> type) {
        ClassDescriptor cd = cache.computeIfAbsent(type, this::getClassDescriptor);

        cd.increaseUsageCount();

        return cd;
    }

    private ClassDescriptor getClassDescriptor(Class<?> type) {
        return new ClassDescriptor(
            type,
            scanAccessible,
            enhancedProperties,
            includeFieldsAsProperties,
            scanStatics,
            propertyFieldPrefix
        );
    }

    /**
     * Creates builder to build {@link ClassIntrospector}.
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link ClassIntrospector}.
     */
    public static final class Builder {

        public boolean scanStatics = false;
        private boolean scanAccessible = true;
        private boolean enhancedProperties = true;
        private boolean includeFieldsAsProperties = true;
        private String propertyFieldPrefix = null;

        private Builder() {}

        public Builder withScanAccessible(boolean scanAccessible) {
            this.scanAccessible = scanAccessible;
            return this;
        }

        public Builder withEnhancedProperties(boolean enhancedProperties) {
            this.enhancedProperties = enhancedProperties;
            return this;
        }

        public Builder withIncludeFieldsAsProperties(boolean includeFieldsAsProperties) {
            this.includeFieldsAsProperties = includeFieldsAsProperties;
            return this;
        }

        public Builder withPropertyFieldPrefix(String propertyFieldPrefix) {
            this.propertyFieldPrefix = propertyFieldPrefix;
            return this;
        }

        public Builder withScanStatics(boolean includeStatics) {
            this.scanStatics = includeStatics;
            return this;
        }

        public ClassIntrospector build() {
            return new ClassIntrospector(this);
        }
    }
}
