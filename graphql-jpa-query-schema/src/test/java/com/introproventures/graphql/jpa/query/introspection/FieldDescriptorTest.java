package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.junit.Test;

import lombok.Data;


public class FieldDescriptorTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector.builder()
                                                                          .withIncludeFieldsAsProperties(true)
                                                                          .withEnhancedProperties(true)
                                                                          .withScanAccessible(true)
                                                                          .withScanStatics(false)
                                                                          .build();

    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(FieldsSampleBean.class);
    
    @Test
    public void testToStringEqualsAndHashCode() {
        FieldDescriptor subject = classDescriptor.getFieldDescriptor("id", true);        
        
        // then
        assertThatCode(() -> {
                            subject.toString();
                            subject.hashCode();
                            subject.equals(subject);
                        }).doesNotThrowAnyException();
        
    }
    
    @Test
    public void testFieldDescriptor() {
        FieldDescriptor subject = classDescriptor.getFieldDescriptor("nickName", true);        
        
        // then
        assertThat(subject).isNotNull()
                           .extracting(FieldDescriptor::getName,
                                       FieldDescriptor::getDeclaringClass,
                                       FieldDescriptor::getRawType,
                                       FieldDescriptor::getRawComponentType,
                                       FieldDescriptor::getRawKeyComponentType,
                                       FieldDescriptor::getGetterRawComponentType,
                                       FieldDescriptor::getGetterRawKeyComponentType,
                                       FieldDescriptor::getSetterRawType,
                                       FieldDescriptor::getSetterRawComponentType)
                           .contains("nickName",
                                     FieldsSampleBean.class,
                                     Optional.class,
                                     String.class,
                                     String.class,
                                     String.class,
                                     String.class,
                                     String.class,
                                     String.class);
    }
    

    @Test
    public void testInvokeGetter() throws InvocationTargetException, IllegalAccessException {
        FieldDescriptor subject = classDescriptor.getFieldDescriptor("id", true);
        FieldsSampleBean target = new FieldsSampleBean("id");
        
        
        // when
        Object result = subject.invokeGetter(target);
        
        // then
        assertThat(result).isEqualTo("id");
    }

    @Test
    public void testInvokeSetter() throws InvocationTargetException, IllegalAccessException {
        FieldDescriptor subject = classDescriptor.getFieldDescriptor("name", true);
        FieldsSampleBean target = new FieldsSampleBean("id");
        
        // when
        subject.invokeSetter(target, "name");
        
        // then
        assertThat(target.getName()).isEqualTo("name");
    }
    

    @Data
    static class FieldsSampleBean {
        private final String id;
        private String name;
        private Optional<String> nickName = Optional.empty();
    }
    
}
