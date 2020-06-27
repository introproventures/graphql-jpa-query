package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThatCode;

import javax.persistence.Entity;

import org.junit.Test;


public class AnnotationDescriptorTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector.builder()
                                                                          .withIncludeFieldsAsProperties(true)
                                                                          .withEnhancedProperties(true)
                                                                          .withScanAccessible(true)
                                                                          .withScanStatics(false)
                                                                          .build();
    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(AnnotationSampeBean.class);

    @Test
    public void testToStringEqualsAndHashCode() {
        AnnotationDescriptor subject = classDescriptor.getAnnotationDescriptor(Entity.class);        
        
        // then
        assertThatCode(() -> {
                            subject.toString();
                            subject.hashCode();
                            subject.equals(subject);
                        }).doesNotThrowAnyException();
        
    }
    
    @Entity
    static class AnnotationSampeBean {
        
    }

}
