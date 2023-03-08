package com.introproventures.graphql.jpa.query.example.books;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;

@Configuration
public class BooksSchemaConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "books")    
    @Qualifier("bookDataSource")    
    public DataSource bookDataSource() {
        return DataSourceBuilder.create()
                .build();
    }    
     
    @Bean
    @Qualifier("bookEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bookEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                           @Qualifier("bookDataSource") DataSource bookDataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        properties.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "true");
        properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");

        return builder
                .dataSource(bookDataSource)
                .packages(Book.class)
                .persistenceUnit("books")
                .properties(properties)
                .build();
    }
    
    @Bean
    public ApplicationRunner booksDataSourceInitializer(@Qualifier("bookDataSource")  DataSource dataSource) {
        return (args) -> {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            
            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
            databasePopulator.addScript(resourceLoader.getResource("books.sql"));
            
            databasePopulator.execute(dataSource);
        };
    }

    @Bean
    @Qualifier("bookEntityManager")
    public SharedEntityManagerBean bookEntityManager(@Qualifier("bookEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        SharedEntityManagerBean bean =  new SharedEntityManagerBean();
        bean.setEntityManagerFactory(entityManagerFactory);
        
        return bean;
    }

    @Bean
    GraphQLSchemaConfigurer booksGraphQLJpaQuerySchemaConfigurer(@Qualifier("bookEntityManager") EntityManager entityManager,
                                                                 GraphQLJpaQueryProperties properties) {
        return registry ->
            registry.register(
                    new GraphQLJpaSchemaBuilder(entityManager)
                        .name("GraphQLBooks")
                        .useDistinctParameter(properties.isUseDistinctParameter())
                        .setDefaultDistinct(properties.isDefaultDistinct())
                        .build()
            );
    };

}
