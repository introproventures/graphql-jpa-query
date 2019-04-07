package com.introproventures.graphql.jpa.query.example.books;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLShemaRegistration;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

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
    @Qualifier("bookEntityManager")
    public LocalContainerEntityManagerFactoryBean bookEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        properties.put(AvailableSettings.HBM2DLL_CREATE_SCHEMAS, "true");
        properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");

        return builder
                .dataSource(bookDataSource())
                .packages(Book.class)
                .persistenceUnit("books")
                .properties(properties)
                .build();
    }
    
    @Bean
    public DataSourceInitializer booksDataSourceInitializer(@Qualifier("bookDataSource")  DataSource bookDataSource) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(resourceLoader.getResource("books.sql"));
        
        dataSourceInitializer.setDataSource(bookDataSource);
        dataSourceInitializer.setDatabasePopulator(databasePopulator);
        
        return dataSourceInitializer;
    }
    

    @Configuration
    public static class GraphQLJpaQuerySchemaConfigurer implements GraphQLSchemaConfigurer {

        private final EntityManager entityManager;

        @Autowired
        private GraphQLJpaQueryProperties properties;

        public GraphQLJpaQuerySchemaConfigurer(@Qualifier("bookEntityManager") EntityManagerFactory entityManager) {
            this.entityManager = entityManager.createEntityManager();
        }

        @Override
        public void configure(GraphQLShemaRegistration registry) {
            registry.register(
                    new GraphQLJpaSchemaBuilder(entityManager)
                        .name("GraphQLBooks")
                        .useDistinctParameter(properties.isUseDistinctParameter())
                        .setDefaultDistinct(properties.isDefautltDistinct())
                        .build()
            );
        }
    }
    
}
