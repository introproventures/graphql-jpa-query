package com.introproventures.graphql.jpa.query.schema.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    /**
     * Test if Java bean property is transient according to JPA specification 
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property has Transient annotation or transient field modifier
     * @throws RuntimeException if property does not exists
     */
    public static boolean isTransient(Class<?> entity, String propertyName) {
        return introspect(entity).getPropertyDescriptor(propertyName)
                                 .map(it -> it.isAnnotationPresent(Transient.class)  
                                                 || it.hasModifier(Modifier::isTransient))
                                 .orElseThrow(() -> new RuntimeException(new NoSuchFieldException(propertyName)));
    }

    /**
     * Test if entity property is annotated with GraphQLIgnore  
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property has GraphQLIgnore
     * @throws RuntimeException if property does not exists
     */
    public static boolean isIgnored(Class<?> entity, String propertyName) {
        return introspect(entity).getPropertyDescriptor(propertyName)
                                 .map(it -> it.isAnnotationPresent(GraphQLIgnore.class))
                                 .orElseThrow(() -> new RuntimeException(new NoSuchFieldException(propertyName)));
    }

    public static class CachedIntrospectionResult {

        private final Map<String, CachedPropertyDescriptor> descriptors;
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
            this.descriptors = Stream.of(beanInfo.getPropertyDescriptors())
                    .map(CachedPropertyDescriptor::new)
                    .collect(Collectors.toMap(CachedPropertyDescriptor::getName, it -> it));
            
            this.fields = iterate((Class) entity, k -> Optional.ofNullable(k.getSuperclass()))
                    .flatMap(k -> Arrays.stream(k.getDeclaredFields()))
                    .filter(f -> descriptors.containsKey(f.getName()))
                    .collect(Collectors.toMap(Field::getName, it -> it));
        }

        public Collection<CachedPropertyDescriptor> getPropertyDescriptors() {
            return descriptors.values();
        }

        public Collection<Field> getFields() {
            return fields.values();
        }
        
        public Optional<CachedPropertyDescriptor> getPropertyDescriptor(String fieldName) {
            return Optional.ofNullable(descriptors.getOrDefault(fieldName, null));
        }

        public boolean hasPropertyDescriptor(String fieldName) {
            return descriptors.containsKey(fieldName);
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
            
            public Optional<Field> getField() {
                return Optional.ofNullable(fields.get(getName()));
            }

            public Optional<Method> getReadMethod() {
                return Optional.ofNullable(delegate.getReadMethod());
            }
            
            public boolean hasModifier(Function<Integer, Boolean> test) {
                return getField().map(it -> test.apply(it.getModifiers()))
                                 .orElse(false);
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
                return isAnnotationPresentOnField(annotation) || isAnnotationPresentOnReadMethod(annotation);
            }

            private boolean isAnnotationPresentOnField(Class<? extends Annotation> annotation) {
                return getField().map(f -> f.isAnnotationPresent(annotation))
                                 .orElse(false);
            }

            private boolean isAnnotationPresentOnReadMethod(Class<? extends Annotation> annotation) {
                return getReadMethod().map(m -> m.isAnnotationPresent(annotation))
                                      .orElse(false);
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
