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

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;

import graphql.GraphQLContext;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;

/**
 * GraphQL JPA Query Example with Spring Boot Autoconfiguration
 * 
 * You can configure GraphQL JPA Query properties in application.yml 
 * 
 * @author Igor Dianov
 *
 */
@SpringBootApplication
@EnableTransactionManagement
public class Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @RequestScope
    public Supplier<GraphQLContext> graphqlContext(HttpServletRequest request) {
        return () -> GraphQLContext.newContext()
                                   .of("request", request)
                                   .of("user", request)
                                   .build();
    }

    @Bean
    @RequestScope
    public Supplier<Instrumentation> instrumentation(HttpServletRequest request) {
        return () -> logger.isDebugEnabled() 
                           ? new TracingInstrumentation() 
                           : SimpleInstrumentation.INSTANCE;
    }
    
}
