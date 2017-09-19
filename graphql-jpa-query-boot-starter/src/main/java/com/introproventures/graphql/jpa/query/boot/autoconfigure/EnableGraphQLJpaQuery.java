package com.introproventures.graphql.jpa.query.boot.autoconfigure;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.introproventures.graphql.jpa.query.boot.autoconfigure.GraphQLJpaQueryAutoConfiguration.DefaultActivitiGraphQLJpaConfiguration;

@Documented
@Retention( RUNTIME )
@Target( TYPE )
@Import(DefaultActivitiGraphQLJpaConfiguration.class)
@PropertySource("classpath:/com/introproventures/graphql/jpa/query/boot/autoconfigure/default.properties")
public @interface EnableGraphQLJpaQuery {
    
}
