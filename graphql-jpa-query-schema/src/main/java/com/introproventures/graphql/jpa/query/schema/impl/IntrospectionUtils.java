package com.introproventures.graphql.jpa.query.schema.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.Transient;

import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

public class IntrospectionUtils {
    private static final Map<Class<?>, CachedIntrospectionResult> map = new LinkedHashMap<>();

    public static CachedIntrospectionResult introspect(Class<?> entity) {
        return map.computeIfAbsent(entity, CachedIntrospectionResult::new);
    }

    public static boolean isTransient(Class<?> entity, String propertyName) {
        if(!introspect(entity).hasPropertyDescriptor(propertyName)) {
            throw new RuntimeException(new NoSuchFieldException(propertyName));
        }
        
        return Stream.of(isAnnotationPresent(entity, propertyName, Transient.class),
                         isModifierPresent(entity, propertyName, Modifier::isTransient))
                     .anyMatch(it -> it.isPresent() && it.get() == true);
    }
    
    public static boolean isIgnored(Class<?> entity, String propertyName) {
        return isAnnotationPresent(entity, propertyName, GraphQLIgnore.class)
                .orElseThrow(() -> new RuntimeException(new NoSuchFieldException(propertyName)));
    }

    private static Optional<Boolean> isAnnotationPresent(Class<?> entity, String propertyName, Class<? extends Annotation> annotation){
        return introspect(entity).getPropertyDescriptor(propertyName)
                                 .map(it -> it.isAnnotationPresent(annotation));
    }

    private static Optional<Boolean> isModifierPresent(Class<?> entity, String propertyName, Function<Integer, Boolean> function){
        return introspect(entity).getField(propertyName)
                                 .map(it -> function.apply(it.getModifiers()));
    }
    
    public static class CachedIntrospectionResult {

        private final Map<String, CachedPropertyDescriptor> map;
        private final Class<?> entity;
        private final BeanInfo beanInfo;
        private final Map<String, Field> fields;
        
        @SuppressWarnings("rawtypes")
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
            
            this.fields = iterate((Class) entity, k -> Optional.ofNullable(k.getSuperclass()))
                    .flatMap(k -> Arrays.stream(k.getDeclaredFields()))
                    .filter(f -> map.containsKey(f.getName()))
                    .collect(Collectors.toMap(Field::getName, it -> it));
        }

        public Collection<CachedPropertyDescriptor> getPropertyDescriptors() {
            return map.values();
        }

        public Optional<CachedPropertyDescriptor> getPropertyDescriptor(String fieldName) {
            return Optional.ofNullable(map.getOrDefault(fieldName, null));
        }

        public boolean hasPropertyDescriptor(String fieldName) {
            return map.containsKey(fieldName);
        }
        
        public Optional<Field> getField(String fieldName) {
            return Optional.ofNullable(fields.get(fieldName));
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

            public Class<?> getPropertyType() {
                return delegate.getPropertyType();
            }
            
            public String getName() {
                return delegate.getName();
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
                return isAnnotationPresentOnField(annotation) || isAnnotationPresentOnReadMethod(annotation);
            }

            private boolean isAnnotationPresentOnField(Class<? extends Annotation> annotation) {
                return Optional.ofNullable(fields.get(delegate.getName()))
                               .map(f -> f.isAnnotationPresent(annotation))
                               .orElse(false);
            }

            private boolean isAnnotationPresentOnReadMethod(Class<? extends Annotation> annotation) {
                return delegate.getReadMethod() != null && delegate.getReadMethod().isAnnotationPresent(annotation);
            }

        }
    }
    
    /**
     * The following method is borrowed from Streams.iterate, 
     * however Streams.iterate is designed to create infinite streams. 
     * 
     * This version has been modified to end when Optional.empty() 
     * is returned from the fetchNextFunction.
     */
    protected static <T> Stream<T> iterate( T seed, Function<T, Optional<T>> fetchNextFunction ) {
        Objects.requireNonNull(fetchNextFunction);

        Iterator<T> iterator = new Iterator<T>() {
            private Optional<T> t = Optional.ofNullable(seed);

            @Override
            public boolean hasNext() {
                return t.isPresent();
            }

            @Override
            public T next() {
                T v = t.get();

                t = fetchNextFunction.apply(v);

                return v;
            }
        };

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize( iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
            false
        );
    }        
}
