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
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.Transient;
import javax.persistence.metamodel.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

public class IntrospectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntrospectionUtils.class);
    
    private static final Map<Class<?>, EntityIntrospectionResult> map = new LinkedHashMap<>();

    public static EntityIntrospectionResult introspect(Class<?> entity) {
        return map.computeIfAbsent(entity, EntityIntrospectionResult::new);
    }

    /**
     * Test if Java bean property is transient according to JPA specification 
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property has Transient annotation or transient field modifier
     * @throws NoSuchElementException if property does not exists
     */
    public static boolean isTransient(Class<?> entity, String propertyName) {
        return introspect(entity).isTransient(propertyName)
                                 .orElseThrow(() -> noSuchElementException(entity, propertyName));
    }
    
    /**
     * Test if Java bean property is persistent according to JPA specification 
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property is persitent
     * @throws NoSuchElementException if property does not exists
     */
    public static boolean isPersistent(Class<?> entity, String propertyName) {
        return !isTransient(entity, propertyName);
    }    

    /**
     * Test if entity property is annotated with GraphQLIgnore  
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property has GraphQLIgnore annotation
     * @throws NoSuchElementException if property does not exists
     */
    public static boolean isIgnored(Class<?> entity, String propertyName) {
        return introspect(entity).isIgnored(propertyName)
                                 .orElseThrow(() -> noSuchElementException(entity, propertyName));
    }
    
    /**
     * Test if entity property is not ignored  
     * 
     * @param entity a Java entity class to introspect
     * @param propertyName the name of the property
     * @return true if property has no GraphQLIgnore annotation
     * @throws NoSuchElementException if property does not exists
     */
    public static boolean isNotIgnored(Class<?> entity, String propertyName) {
        return !isIgnored(entity, propertyName);
    }

    public static class EntityIntrospectionResult {

        private final Map<String, AttributePropertyDescriptor> descriptors;
        private final Class<?> entity;
        private final BeanInfo beanInfo;
        private final Map<String, FieldDescriptor> fields;
        
        public EntityIntrospectionResult(Class<?> entity) {
            try {
                this.beanInfo = Introspector.getBeanInfo(entity);
            } catch (IntrospectionException cause) {
                throw new RuntimeException(cause);
            }

            this.entity = entity;
            
            this.fields = getClasses().filter(cls -> !Object.class.equals(cls))
                                      .flatMap(k -> Arrays.stream(k.getDeclaredFields()))
                                      .filter(f -> !Modifier.isStatic(f.getModifiers()))
                                      .map(FieldDescriptor::new)
                                      .collect(Collectors.toMap(FieldDescriptor::getName,
                                                                Function.identity(),
                                                                fieldMergeFunction()));

            this.descriptors = Stream.of(beanInfo.getPropertyDescriptors())
                                  .map(AttributePropertyDescriptor::new)
                                  .collect(Collectors.toMap(AttributePropertyDescriptor::getName, 
                                                            Function.identity()));
        }

        public Optional<Boolean> isIgnored(String propertyName) {
            return getFieldDescriptor(propertyName).map(FieldDescriptor::isIgnored)
                                                   .filter(Boolean::valueOf).map(Optional::of)
                                                   .orElseGet(() -> getPropertyDescriptor(propertyName).map(AttributePropertyDescriptor::isIgnored));
        }
        
        public Collection<FieldDescriptor> getFieldDescriptors() {
            return fields.values();
        }

        public Collection<AttributePropertyDescriptor> getPropertyDescriptors() {
            return descriptors.values();
        }

        public Optional<AttributePropertyDescriptor> getPropertyDescriptor(String fieldName) {
            return Optional.ofNullable(descriptors.get(Introspector.decapitalize(fieldName)));
        }
        
        public Optional<AttributePropertyDescriptor> getPropertyDescriptor(Attribute<?,?> attribute) {
            return getPropertyDescriptor(attribute.getName());
        }

        public boolean hasPropertyDescriptor(String fieldName) {
            return descriptors.containsKey(fieldName);
        }
        
        public Optional<FieldDescriptor> getFieldDescriptor(String fieldName) {
            return Optional.ofNullable(fields.get(fieldName));
        }
        
        public Optional<Boolean> isTransient(String propertyName) {
            return getFieldDescriptor(propertyName).map(FieldDescriptor::isTransient)
                                                   .filter(Boolean::valueOf).map(Optional::of)
                                                   .orElseGet(() -> getPropertyDescriptor(propertyName).map(AttributePropertyDescriptor::isTransient));
        }
        
        public Class<?> getEntity() {
            return entity;
        }

        public BeanInfo getBeanInfo() {
            return beanInfo;
        }
        
        public Optional<String> getSchemaDescription() {
            return getClasses().filter(cls -> !Object.class.equals(cls))
                               .map(cls -> Optional.ofNullable(cls.getAnnotation(GraphQLDescription.class))
                                                   .map(GraphQLDescription::value))
                               .filter(Optional::isPresent)
                               .map(Optional::get)
                               .findFirst();                        
        }
        
        public boolean hasSchemaDescription() {
            return getSchemaDescription().isPresent();                  
        }
        
        public Optional<String> getSchemaDescription(String propertyName) {
            return getPropertyDescriptor(propertyName).flatMap(AttributePropertyDescriptor::getSchemaDescription).map(Optional::of)
                                                      .orElseGet(() -> getFieldDescriptor(propertyName).flatMap(FieldDescriptor::getSchemaDescription));
        }
        
        public Stream<Class<?>> getClasses() {
            return iterate(entity, k -> Optional.ofNullable(k.getSuperclass()));
        }
        
        private BinaryOperator<FieldDescriptor> fieldMergeFunction() {
            return (field1, field2) -> {
                LOGGER.warn("Duplicated field " + field1.getName() + " found in " + field1.getDeclaringClass() + " and " + field2.getDeclaringClass());
                return field1;
            };
        }
        
        public class AttributePropertyDescriptor {

            private final PropertyDescriptor delegate;

            public AttributePropertyDescriptor(PropertyDescriptor delegate) {
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

            public Optional<Method> getReadMethod() {
                return Optional.ofNullable(delegate.getReadMethod());
            }
            
            public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
                return getReadMethod().map(m -> m.getAnnotation(annotationClass));
            }
            
            public Optional<String> getSchemaDescription() {
                return getAnnotation(GraphQLDescription.class).map(GraphQLDescription::value);
            }
            
            public boolean hasSchemaDescription() {
                return getSchemaDescription().isPresent();
            }
            
            public boolean isTransient() {
                return isAnnotationPresent(Transient.class);
            }

            public boolean isIgnored() {
                return isAnnotationPresent(GraphQLIgnore.class);  
            }
            
            public boolean isNotIgnored() {
                return !isIgnored();  
            }
            
            public boolean hasReadMethod() {
                return getReadMethod().isPresent();
            }
            
            public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
                return getReadMethod().map(m -> m.isAnnotationPresent(annotation))
                                      .orElse(false);
            }

            @Override
            public String toString() {
                return "AttributePropertyDescriptor [delegate=" + delegate + "]";
            }

            private EntityIntrospectionResult getEnclosingInstance() {
                return EntityIntrospectionResult.this;
            }
            
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + Objects.hash(delegate);
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                AttributePropertyDescriptor other = (AttributePropertyDescriptor) obj;
                if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                    return false;
                return Objects.equals(delegate, other.delegate);
            }
        }
        
        public class FieldDescriptor {

            private final Field delegate;

            public FieldDescriptor(Field delegate) {
                this.delegate = delegate;
            }

            public Field getDelegate() {
                return delegate;
            }

            public Class<?> getDeclaringClass() {
                return delegate.getDeclaringClass();
            }
            
            public Class<?> getFieldType() {
                return delegate.getClass();
            }
            
            public String getName() {
                return delegate.getName();
            }
            
            public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
                return Optional.ofNullable(delegate.getAnnotation(annotationClass));
            }

            public Collection<FieldDescriptor> getFieldDescriptors() {
                return fields.values();
            }
            
            public Optional<String> getSchemaDescription() {
                return getAnnotation(GraphQLDescription.class).map(GraphQLDescription::value);
            }
            
            public boolean hasSchemaDescription() {
                return getSchemaDescription().isPresent();
            }
            
            public boolean isTransient() {
                return isAnnotationPresent(Transient.class)  
                        || hasFieldModifier(Modifier::isTransient);
            }

            public boolean isIgnored() {
                return isAnnotationPresent(GraphQLIgnore.class);  
            }
            
            public boolean isNotIgnored() {
                return !isIgnored();  
            }
            
            
            public boolean hasFieldModifier(Function<Integer, Boolean> test) {
                return test.apply(delegate.getModifiers());
            }

            public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
                return getAnnotation(annotation).isPresent();
            }

            @Override
            public String toString() {
                return "FieldDescriptor [delegate=" + delegate + "]";
            }

            private EntityIntrospectionResult getEnclosingInstance() {
                return EntityIntrospectionResult.this;
            }
            
            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + getEnclosingInstance().hashCode();
                result = prime * result + Objects.hash(delegate);
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                FieldDescriptor other = (FieldDescriptor) obj;
                if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                    return false;
                return Objects.equals(delegate, other.delegate);
            }
        }
        
        
        @Override
        public int hashCode() {
            return Objects.hash(beanInfo, entity);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EntityIntrospectionResult other = (EntityIntrospectionResult) obj;
            return Objects.equals(beanInfo, other.beanInfo) && Objects.equals(entity, other.entity);
        }
        
        @Override
        public String toString() {
            return "EntityIntrospectionResult [beanInfo=" + beanInfo + "]";
        }
    }
    
    /**
     * The following method is borrowed from Streams.iterate, 
     * however Streams.iterate is designed to create infinite streams. 
     * 
     * This version has been modified to end when Optional.empty() 
     * is returned from the fetchNextFunction.
     */
    public static <T> Stream<T> iterate(T seed, Function<T,Optional<T>> fetchNextFunction) {
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
    
    private static NoSuchElementException noSuchElementException(Class<?> containerClass,
                                                                 String propertyName) {
        return new NoSuchElementException(String.format(Locale.ROOT,
                                                        "Could not locate field name [%s] on class [%s]",
                                                        propertyName,
                                                        containerClass.getName()));
        
    }
    
}
