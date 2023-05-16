package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThatCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class FieldsTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector
        .builder()
        .withIncludeFieldsAsProperties(true)
        .withEnhancedProperties(true)
        .withScanAccessible(true)
        .withScanStatics(false)
        .build();
    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(FieldsSampeBean.class);

    @Test
    public void testToStringEqualsAndHashCode() {
        Fields subject = classDescriptor.getFields();

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
    static class FieldsSampeBean {

        private String foo;
        private String bar;
    }
}
