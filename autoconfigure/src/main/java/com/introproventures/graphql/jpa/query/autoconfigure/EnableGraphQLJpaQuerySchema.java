package com.introproventures.graphql.jpa.query.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EntityScan
@Import(EnableGraphQLJpaQuerySchemaImportSelector.class)
@ImportAutoConfiguration(GraphQLSchemaBuilderAutoConfiguration.class)
public @interface EnableGraphQLJpaQuerySchema {
    @AliasFor(annotation = EntityScan.class, attribute = "basePackageClasses")
    Class<?>[] value() default {};

    @AliasFor(annotation = EntityScan.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};
}
