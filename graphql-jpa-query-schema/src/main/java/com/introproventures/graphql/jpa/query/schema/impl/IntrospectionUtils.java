package com.introproventures.graphql.jpa.query.schema.impl;

import static java.util.Locale.ENGLISH;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;
import com.introproventures.graphql.jpa.query.annotation.GraphQLIgnore;

public class IntrospectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntrospectionUtils.class);
    
    private static final Map<Class<?>, EntityIntrospectionResult> map = new LinkedHashMap<>();

    public static EntityIntrospectionResult introspect(Class<?> entity) {
        return Optional.ofNullable(map.get(entity))
                       .orElseThrow(() -> new NoSuchElementException(entity.getName()));
    }
    
    public static EntityIntrospectionResult introspect(ManagedType<?> entityType) {
        return map.computeIfAbsent(entityType.getJavaType(), 
                                   cls -> new EntityIntrospectionResult(entityType));
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
        return introspect(entity).isTransient(propertyName);
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
        return introspect(entity).isIgnored(propertyName);
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
        private final ManagedType<?> managedType;
        private final Map<String, Attribute<?,?>> attributes;

        public EntityIntrospectionResult(ManagedType<?> managedType) {
            this.managedType = managedType;
            
            this.attributes = managedType.getAttributes()
                                    .stream()
                                    .collect(Collectors.toMap(Attribute::getName, 
                                                              Function.identity()));

            this.entity = managedType.getJavaType();
            
            try {
                this.beanInfo = Introspector.getBeanInfo(entity);
            } catch (IntrospectionException cause) {
                throw new RuntimeException(cause);
            }
            
            this.descriptors = Stream.of(beanInfo.getPropertyDescriptors())
                                     .filter(it -> !"class".equals(it.getName()))  
                                     .map(AttributePropertyDescriptor::new)
                                     .collect(Collectors.toMap(AttributePropertyDescriptor::getName, 
                                                                Function.identity()));
        }
        
        public Collection<AttributePropertyDescriptor> getTransientPropertyDescriptors() {
            return descriptors.values()
                              .stream()
                              .filter(AttributePropertyDescriptor::isTransient)
                              .collect(Collectors.toList());
        }

        public Collection<AttributePropertyDescriptor> getPersistentPropertyDescriptors() {
            return descriptors.values()
                              .stream()
                              .filter(AttributePropertyDescriptor::isPersistent)
                              .collect(Collectors.toList());
        }
        
        public Collection<AttributePropertyDescriptor> getIgnoredPropertyDescriptors() {
            return descriptors.values()
                              .stream()
                              .filter(AttributePropertyDescriptor::isIgnored)
                              .collect(Collectors.toList());
        }
        
        public Map<String, Attribute<?, ?>> getAttributes() {
            return attributes;
        }
        
        public Boolean isIgnored(String propertyName) {
            return getPropertyDescriptor(propertyName).map(AttributePropertyDescriptor::isIgnored)
                                                      .orElseThrow(() -> noSuchElementException(entity, propertyName));
        }

        public Boolean isNotIgnored(String propertyName) {
            return getPropertyDescriptor(propertyName).map(AttributePropertyDescriptor::isNotIgnored)
                                                      .orElseThrow(() -> noSuchElementException(entity, propertyName));
        }
        
        public Collection<AttributePropertyDescriptor> getPropertyDescriptors() {
            return descriptors.values();
        }

        public Optional<AttributePropertyDescriptor> getPropertyDescriptor(String fieldName) {
            return Optional.ofNullable(descriptors.get(fieldName));
        }
        
        public Optional<AttributePropertyDescriptor> getPropertyDescriptor(Attribute<?,?> attribute) {
            return getPropertyDescriptor(attribute.getName());
        }

        public boolean hasPropertyDescriptor(String fieldName) {
            return descriptors.containsKey(fieldName);
        }
        
        public Boolean isTransient(String propertyName) {
            return getPropertyDescriptor(propertyName).map(AttributePropertyDescriptor::isTransient)
                                                      .orElseThrow(() -> noSuchElementException(entity, propertyName));
        }
        
        public Class<?> getEntity() {
            return entity;
        }

        public ManagedType<?> getManagedType() {
            return managedType;
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
            return getPropertyDescriptor(propertyName).flatMap(AttributePropertyDescriptor::getSchemaDescription);
        }
        
        public Stream<Class<?>> getClasses() {
            return iterate(entity, k -> Optional.ofNullable(k.getSuperclass()));
        }
        
        public class AttributePropertyDescriptor {

            private final PropertyDescriptor delegate;
            private final Optional<Attribute<?,?>> attribute;
            private final Optional<Field> field;

            public AttributePropertyDescriptor(PropertyDescriptor delegate) {
                this.delegate = delegate;
                
                String name = delegate.getName();
                
                this.attribute = Optional.ofNullable(attributes.getOrDefault(name, 
                                                                             attributes.get(capitalize(name))));
                this.field = attribute.map(Attribute::getJavaMember)
                                      .filter(Field.class::isInstance)
                                      .map(Field.class::cast)
                                      .map(Optional::of).orElseGet(() -> findField(entity, name));
            }

            public ManagedType<?> getManagedType() {
                return managedType;
            }
            
            public PropertyDescriptor getDelegate() {
                return delegate;
            }
            
            public Class<?> getPropertyType() {
                return delegate.getPropertyType();
            }
            
            public String getName() {
                return attribute.map(Attribute::getName)
                                .orElseGet(() -> delegate.getName());
            }

            public Optional<Method> getReadMethod() {
                return Optional.ofNullable(delegate.getReadMethod());
            }
            
            public <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationClass) {
                return getReadMethod().map(m -> m.getAnnotation(annotationClass))
                                      .map(Optional::of).orElseGet(() -> getField().map(f -> f.getAnnotation(annotationClass)));
            }
            
            public Optional<Attribute<?,?>> getAttribute() {
                return attribute;
            }
            
            public Optional<Field> getField() {
                return field;
            }
            
            public Optional<String> getSchemaDescription() {
                return getAnnotation(GraphQLDescription.class).map(GraphQLDescription::value);
            }
            
            public boolean hasSchemaDescription() {
                return getSchemaDescription().isPresent();
            }
            
            public boolean isTransient() {
                return !attribute.isPresent();
            }

            public boolean isPersistent() {
                return attribute.isPresent();
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
                return getAnnotation(annotation).isPresent();
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
    
    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }
    
    private static Optional<Field> findField(Class<?> containerClass, String propertyName) {
        if ( containerClass == null ) {
            throw new IllegalArgumentException( "Class on which to find field [" + propertyName + "] cannot be null" );
        }
        else if ( containerClass == Object.class ) {
            throw new IllegalArgumentException( "Illegal attempt to locate field [" + propertyName + "] on Object.class" );
        }

        return Optional.ofNullable(locateField( containerClass, propertyName ));
    }

    private static Field locateField(Class<?> clazz, String propertyName) {
        if ( clazz == null || Object.class.equals( clazz ) ) {
            return null;
        }

        try {
            Field field = clazz.getDeclaredField( propertyName );
            if ( !isStaticField( field ) ) {
                return field;
            }
            return locateField( clazz.getSuperclass(), propertyName );
        }
        catch ( NoSuchFieldException nsfe ) {
            return locateField( clazz.getSuperclass(), propertyName );
        }
    }

    private static boolean isStaticField(Field field) {
        return field != null && ( field.getModifiers() & Modifier.STATIC ) == Modifier.STATIC;
    }    
}
