package com.introproventures.graphql.jpa.query.graphiql;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication
public class WebMvcViewControllerConfigurer {

    @Bean
    @ConditionalOnMissingBean(name = "graphiqlViewController")
    public WebMvcConfigurer graphiqlViewController() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/graphiql")
                        .setViewName("forward:/graphiql/index.html");
            }
        };
    }
}