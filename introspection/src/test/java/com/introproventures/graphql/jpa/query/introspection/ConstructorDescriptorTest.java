package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class ConstructorDescriptorTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector
        .builder()
        .withIncludeFieldsAsProperties(true)
        .withEnhancedProperties(true)
        .withScanAccessible(true)
        .withScanStatics(false)
        .build();
    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(SampleBean.class);

    @Test
    public void testToStringEqualsHashCode() {
        ConstructorDescriptor subject = classDescriptor.getConstructorDescriptor(new Class[] {}, true);

        // then
        assertThatCode(() -> {
                subject.toString();
                subject.hashCode();
                subject.equals(subject);
            })
            .doesNotThrowAnyException();
    }

    @Test
    public void testGetDeclaringClass() {
        ConstructorDescriptor subject = classDescriptor.getConstructorDescriptor(new Class[] {}, true);

        // then
        assertThat(subject.getDeclaringClass()).isEqualTo(SampleBean.class);
    }

    @Test
    public void testParameters() {
        ConstructorDescriptor subject = classDescriptor.getConstructorDescriptor(new Class[] {}, true);

        // then
        assertThat(subject.getParameters()).isEqualTo(new Class[] {});
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class SampleBean {

        private String foo;
        private String bar;
    }
}
