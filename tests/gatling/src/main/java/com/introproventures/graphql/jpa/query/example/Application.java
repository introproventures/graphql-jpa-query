/*
 * Copyright 2017 IntroPro Ventures, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.introproventures.graphql.jpa.query.example;

import static graphql.schema.GraphQLScalarType.newScalar;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJPASchemaBuilderCustomizer;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import java.util.Date;
import java.util.Optional;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.VariableValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Application {

    //  docker run -it --rm -p 5432:5432 -e POSTGRES_PASSWORD=password postgres
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableGraphQLJpaQuerySchema(basePackageClasses = ProcessInstanceEntity.class)
    static class Config {

        @Bean
        GraphQLJPASchemaBuilderCustomizer graphQLJPASchemaBuilderCustomizer(
            @Value("${activiti.cloud.graphql.jpa-query.date-format:yyyy-MM-dd'T'HH:mm:ss.SSSX}") String dateFormatString
        ) {
            return builder ->
                builder
                    .name("Query")
                    .description("Activiti Cloud Query Schema")
                    .enableAggregate(true)
                    .scalar(
                        VariableValue.class,
                        newScalar()
                            .name("VariableValue")
                            .description("VariableValue type")
                            .coercing(
                                new JavaScalars.GraphQLObjectCoercing() {
                                    public Object serialize(final Object input) {
                                        return Optional
                                            .ofNullable(input)
                                            .filter(VariableValue.class::isInstance)
                                            .map(VariableValue.class::cast)
                                            .map(it -> Optional.ofNullable(it.getValue()).orElse("null"))
                                            .orElse(input);
                                    }
                                }
                            )
                            .build()
                    )
                    .scalar(
                        Date.class,
                        newScalar()
                            .name("Date")
                            .description("Date type with '" + dateFormatString + "' format")
                            .coercing(new JavaScalars.GraphQLDateCoercing(dateFormatString))
                            .build()
                    );
        }
    }
}
