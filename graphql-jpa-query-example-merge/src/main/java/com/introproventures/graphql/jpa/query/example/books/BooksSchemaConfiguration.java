package com.introproventures.graphql.jpa.query.example.books;

import java.util.HashMap;
import javax.sql.DataSource;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJpaQueryProperties;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.book.Book;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BooksSchemaConfiguration {

    @Bean
    PlatformTransactionManager bookTransactionManager() {
        return new JpaTransactionManager(bookEntityManagerFactory().getObject());
    }

    @Bean
    @ConfigurationProperties(prefix = "books")    
    public DataSource bookDataSource() {
        return DataSourceBuilder.create()
                .build();
    }
     
    @Bean
    public LocalContainerEntityManagerFactoryBean bookEntityManagerFactory() {
        var properties = new HashMap<String, Object>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        properties.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "true");
        properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        var factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(bookDataSource());
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        factoryBean.setPackagesToScan(Book.class.getPackage().getName());
        factoryBean.setJpaPropertyMap(properties);

        return factoryBean;
    }
    
    @Bean
    public ApplicationRunner booksDataSourceInitializer() {
        return (args) -> {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            
            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
            databasePopulator.addScript(resourceLoader.getResource("books.sql"));
            
            databasePopulator.execute(bookDataSource());
        };
    }

    @Bean
    public SharedEntityManagerBean bookEntityManager() {
        SharedEntityManagerBean bean =  new SharedEntityManagerBean();
        bean.setEntityManagerFactory(bookEntityManagerFactory().getObject());
        
        return bean;
    }

    @Bean
    GraphQLSchemaConfigurer booksGraphQLJpaQuerySchemaConfigurer(GraphQLJpaQueryProperties properties) {
        return registry ->
            registry.register(
                    new GraphQLJpaSchemaBuilder(bookEntityManager().getObject())
                        .name("GraphQLBooks")
                        .useDistinctParameter(properties.isUseDistinctParameter())
                        .setDefaultDistinct(properties.isDefaultDistinct())
                        .build()
            );
    };

}
