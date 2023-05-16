package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThatCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class AnnotationsTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector
        .builder()
        .withIncludeFieldsAsProperties(true)
        .withEnhancedProperties(true)
        .withScanAccessible(true)
        .withScanStatics(false)
        .build();
    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(AnnotationsSampeBean.class);

    @Test
    public void testToStringEqualsAndHashCode() {
        Annotations subject = classDescriptor.getAnnotations();

        // then
        assertThatCode(() -> {
                subject.toString();
                subject.hashCode();
                subject.equals(subject);
            })
            .doesNotThrowAnyException();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class AnnotationsSampeBean {

        private String foo;
        private String bar;
    }
}
