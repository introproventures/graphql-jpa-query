package com.introproventures.graphql.jpa.query.graphiql;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication
public class WebMvcViewControllerConfigurer {

    @Bean
    public WebMvcConfigurer graphqiQLRedirectToIndex() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
            	registry.addRedirectViewController("/graphiql", "graphiql/index.html");
            }
        };
    }
}