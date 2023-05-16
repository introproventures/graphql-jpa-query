package com.introproventures.graphql.jpa.query.introspection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import lombok.Data;
import org.junit.jupiter.api.Test;

public class MethodDescriptorTest {

    private static ClassIntrospector classIntrospector = ClassIntrospector
        .builder()
        .withIncludeFieldsAsProperties(true)
        .withEnhancedProperties(true)
        .withScanAccessible(true)
        .withScanStatics(false)
        .build();

    // given
    private ClassDescriptor classDescriptor = classIntrospector.introspect(MethodsSampleBean.class);

    @Test
    public void testToStringEqualsAndHashCode() {
        MethodDescriptor subject = classDescriptor.getMethodDescriptor("getId", true);

        // then
        assertThatCode(() -> {
                subject.toString();
                subject.hashCode();
                subject.equals(subject);
            })
            .doesNotThrowAnyException();
    }

    @Test
    public void testGetMethodDescriptor() {
        MethodDescriptor subject = classDescriptor.getMethodDescriptor("getNickName", true);

        // then
        assertThat(subject)
            .isNotNull()
            .extracting(
                MethodDescriptor::getName,
                MethodDescriptor::getDeclaringClass,
                MethodDescriptor::getGetterRawType,
                MethodDescriptor::getGetterRawComponentType,
                MethodDescriptor::getGetterRawKeyComponentType,
                MethodDescriptor::getRawParameterTypes,
                MethodDescriptor::getRawReturnType
            )
            .contains(
                "getNickName",
                MethodsSampleBean.class,
                Optional.class,
                String.class,
                String.class,
                Optional.class,
                String.class,
                String.class
            );
    }

    @Test
    public void testSetMethodDescriptor() throws NoSuchMethodException, SecurityException {
        MethodDescriptor subject = classDescriptor.getMethodDescriptor("setNickName", true);
        Method method = MethodsSampleBean.class.getDeclaredMethod("setNickName", new Class[] { Optional.class });

        // then
        assertThat(subject)
            .isNotNull()
            .extracting(
                MethodDescriptor::getName,
                MethodDescriptor::getMethod,
                MethodDescriptor::isPublic,
                MethodDescriptor::getDeclaringClass,
                MethodDescriptor::getGetterRawType,
                MethodDescriptor::getGetterRawComponentType,
                MethodDescriptor::getGetterRawKeyComponentType,
                MethodDescriptor::getRawParameterTypes,
                MethodDescriptor::getRawReturnType
            )
            .contains(
                "setNickName",
                method,
                true,
                MethodsSampleBean.class,
                void.class,
                null,
                null,
                new Class[] { Optional.class },
                void.class
            );
    }

    @Test
    public void testInvokeGetter() throws InvocationTargetException, IllegalAccessException {
        MethodDescriptor subject = classDescriptor.getMethodDescriptor("getId", true);
        MethodsSampleBean target = new MethodsSampleBean("id");

        // when
        Object result = subject.invokeGetter(target);

        // then
        assertThat(result).isEqualTo("id");
    }

    @Test
    public void testInvokeSetter() throws InvocationTargetException, IllegalAccessException {
        MethodDescriptor subject = classDescriptor.getMethodDescriptor("setName", true);
        MethodsSampleBean target = new MethodsSampleBean("id");

        // when
        subject.invokeSetter(target, "name");

        // then
        assertThat(target.getName()).isEqualTo("name");
    }

    @Data
    static class MethodsSampleBean {

        private final String id;
        private String name;
        private Optional<String> nickName = Optional.empty();
    }
}
