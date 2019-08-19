package com.introproventures.graphql.jpa.query.schema.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Transient;

public class IntrospectionUtils {
    private static final Map<Class<?>, CachedIntrospectionResult> map = new LinkedHashMap<>();

    public static CachedIntrospectionResult introspect(Class<?> entity) {
        return map.computeIfAbsent(entity, CachedIntrospectionResult::new);
    }

    public static boolean isTransient(Class<?> entity, String propertyName) {
        return introspect(entity).getPropertyDescriptor(propertyName)
                .map(it -> it.isAnnotationPresent(Transient.class))
                .orElseThrow(() -> new RuntimeException(new NoSuchFieldException(propertyName)));
    }

    public static class CachedIntrospectionResult {

        private final Map<String, CachedPropertyDescriptor> map;
        private final Class<?> entity;
        private final BeanInfo beanInfo;

        public CachedIntrospectionResult(Class<?> entity) {
            try {
                this.beanInfo = Introspector.getBeanInfo(entity);
            } catch (IntrospectionException cause) {
                throw new RuntimeException(cause);
            }

            this.entity = entity;
            this.map = Stream.of(beanInfo.getPropertyDescriptors())
                    .map(CachedPropertyDescriptor::new)
                    .collect(Collectors.toMap(CachedPropertyDescriptor::getName, it -> it));
        }

        public Collection<CachedPropertyDescriptor> getPropertyDescriptors() {
            return map.values();
        }

        public Optional<CachedPropertyDescriptor> getPropertyDescriptor(String fieldName) {
            return Optional.ofNullable(map.getOrDefault(fieldName, null));
        }

        public Class<?> getEntity() {
            return entity;
        }

        public BeanInfo getBeanInfo() {
            return beanInfo;
        }

        public class CachedPropertyDescriptor {
            private final PropertyDescriptor delegate;

            public CachedPropertyDescriptor(PropertyDescriptor delegate) {
                this.delegate = delegate;
            }

            public PropertyDescriptor getDelegate() {
                return delegate;
            }

            public String getName() {
                return delegate.getName();
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
                return isAnnotationPresentOnField(annotation) || isAnnotationPresentOnReadMethod(annotation);
            }

            private boolean isAnnotationPresentOnField(Class<? extends Annotation> annotation) {
                try {
                    return entity.getDeclaredField(delegate.getName()).isAnnotationPresent(annotation);
                } catch (NoSuchFieldException e) {
                    return false;
                }
            }

            private boolean isAnnotationPresentOnReadMethod(Class<? extends Annotation> annotation) {
                return delegate.getReadMethod() != null && delegate.getReadMethod().isAnnotationPresent(annotation);
            }

        }
    }
}
