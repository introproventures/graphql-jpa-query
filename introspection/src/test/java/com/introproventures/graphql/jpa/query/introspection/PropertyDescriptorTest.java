package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.persistence.Version;
import java.util.Optional;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class PropertyDescriptorTest {

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
    public void testToString() {
        PropertyDescriptor subject = classDescriptor.getPropertyDescriptor("name", true);

        // then
        assertThatCode(() -> {
                subject.toString();
                subject.hashCode();
                subject.equals(subject);
            })
            .doesNotThrowAnyException();
    }

    @Test
    public void testGetField() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true).getField()).isNotNull();
    }

    @Test
    public void testGetGetter() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true).getGetter(true)).isNotNull();
    }

    @Test
    public void testGetSetter() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("name", true).getSetter(true)).isNotNull();
    }

    @Test
    public void testResolveKeyType() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("optional", true).resolveKeyType(true))
            .isEqualTo(String.class);
    }

    @Test
    public void testResolveComponentType() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("optional", true).resolveComponentType(true))
            .isEqualTo(String.class);
    }

    @Test
    public void testIsFieldOnlyDescriptor() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("version", true).isFieldOnlyDescriptor()).isFalse();
    }

    @Test
    public void testGetAnnotations() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("version", true).getAnnotations()).isNotNull();
    }

    @Test
    public void testGetAnnotationDescriptor() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("version", true).getAnnotationDescriptor(Version.class))
            .isNotNull();
    }

    @Test
    public void testGetAnnotation() {
        // then
        assertThat(classDescriptor.getPropertyDescriptor("version", true).getAnnotation(Version.class)).isNotNull();
    }

    @Data
    static class SampleBean {

        private final String id;

        @Version
        private Integer version;

        private String name;
        private Optional<String> optional;
    }
}
