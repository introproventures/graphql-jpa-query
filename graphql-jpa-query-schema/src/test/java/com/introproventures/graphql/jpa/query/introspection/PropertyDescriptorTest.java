package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Optional;

import org.junit.Test;

import lombok.Data;


public class PropertyDescriptorTest {
    
    private static ClassIntrospector classIntrospector = ClassIntrospector.builder()
                                                                   .withIncludeFieldsAsProperties(true)
                                                                   .withEnhancedProperties(true)
                                                                   .withScanAccessible(true)
                                                                   .withScanStatics(false)
                                                                   .build();

    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(SampleBean.class);
    
    @Test
    public void testToString() {
        PropertyDescriptor subject = classDescriptor.getPropertyDescriptor("name", true);        
        
        // then
        assertThatCode(() -> {
                            subject.toString();
                            subject.hashCode();
                            subject.equals(subject);
                        }).doesNotThrowAnyException();
    }
    
    @Test
    public void testGetField() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true)
                                  .getField())
                                  .isNotNull();
                                            
    }

    @Test
    public void testGetGetter() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true)
                                  .getGetter(true))
                                  .isNotNull();
                                            
    }
    
    @Test
    public void testGetSetter() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true)
                                  .getSetter(true))
                                  .isNotNull();
                                            
    }
    
    @Test
    public void testResolveKeyType() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("optional", true)
                                  .resolveKeyType(true))
                                  .isEqualTo(String.class);
                                            
    }
    
    @Test
    public void testResolveComponentType() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("optional", true)
                                  .resolveComponentType(true))
                                  .isEqualTo(String.class);
                                            
    }
    
    @Test
    public void testIsFieldOnlyDescriptor() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("version", true)
                                  .isFieldOnlyDescriptor())
                                  .isFalse();
                                            
    }
    
    
    @Data
    static class SampleBean {
        private final String id;
        private Integer version;
        private String name;
        private Optional<String> optional;
    }

}
