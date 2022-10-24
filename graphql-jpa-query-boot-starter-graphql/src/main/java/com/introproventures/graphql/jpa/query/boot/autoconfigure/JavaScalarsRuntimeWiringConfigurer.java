package com.introproventures.graphql.jpa.query.boot.autoconfigure;

import com.introproventures.graphql.jpa.query.autoconfigure.JavaScalarsWiringPostProcessor;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

public class JavaScalarsRuntimeWiringConfigurer implements RuntimeWiringConfigurer {
    @Override
    public void configure(RuntimeWiring.Builder wiringBuilder) {
        JavaScalars.scalars()
                   .forEach(wiringBuilder::scalar);

        wiringBuilder.transformer(new JavaScalarsWiringPostProcessor());
    }
}
