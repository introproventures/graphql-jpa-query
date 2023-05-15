package com.introproventures.graphql.jpa.query.introspection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;


public class ConstructorsTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector.builder()
                                                                          .withIncludeFieldsAsProperties(true)
                                                                          .withEnhancedProperties(true)
                                                                          .withScanAccessible(true)
                                                                          .withScanStatics(false)
                                                                          .build();
    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(ConstructorsSampeBean.class);
    
    @Test
    public void testToStringEqualsAndHashCode() {
        Constructors subject = classDescriptor.getConstructors();        
        
        // then
        assertThatCode(() -> {
                            subject.toString();
                            subject.hashCode();
                            subject.equals(subject);
                        }).doesNotThrowAnyException();
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ConstructorsSampeBean {
        private String foo;
        private String bar;
        
    }

}
