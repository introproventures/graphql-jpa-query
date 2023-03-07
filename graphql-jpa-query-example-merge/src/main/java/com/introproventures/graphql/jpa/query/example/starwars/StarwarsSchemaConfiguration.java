package com.introproventures.graphql.jpa.query.example.starwars;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLShemaRegistration;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.starwars.Droid;
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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.SharedEntityManagerBean;

@Configuration
public class StarwarsSchemaConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "starwars")    
    @Qualifier("starWarsDataSource")
    public DataSource starWarsDataSource() {
        return DataSourceBuilder.create().build();
    }    
     
    @Bean
    public ApplicationRunner  starWarsDataSourceInitializer(@Qualifier("starWarsDataSource") DataSource starWarsDataSource) {
        return (args) -> {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            
            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
            databasePopulator.addScript(resourceLoader.getResource("starwars.sql"));
            
            databasePopulator.execute(starWarsDataSource);
        };
    }
    
    
    @Bean
    @Primary
    @Qualifier("starWarsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean starWarsEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               @Qualifier("starWarsDataSource") DataSource starWarsDataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        properties.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "true");
        properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");
        
        return builder
                .dataSource(starWarsDataSource)
                .packages(Droid.class)
                .persistenceUnit("starwars")
                .properties(properties)
                .build();
    }    

    @Bean 
    @Qualifier("starWarsEntityManager")
    public SharedEntityManagerBean starWarsEntityManager(@Qualifier("starWarsEntityManagerFactory") EntityManagerFactory entityManager) {
        SharedEntityManagerBean bean =  new SharedEntityManagerBean();
        bean.setEntityManagerFactory(entityManager);
        
        return bean;
    }    
    @Configuration
    public static class GraphQLJpaQuerySchemaConfigurer implements GraphQLSchemaConfigurer {

        private final EntityManager entityManager;

        public GraphQLJpaQuerySchemaConfigurer(@Qualifier("starWarsEntityManager") EntityManager starWarsEntityManager,
                                               @Qualifier("starWarsEntityManager") EntityManager starWarsEntit) {
            this.entityManager = starWarsEntityManager;
        }

        @Override
        public void configure(GraphQLShemaRegistration registry) {

            registry.register(new GraphQLJpaSchemaBuilder(entityManager).name("GraphQLStarWars").build());
        }
    }
    
}
