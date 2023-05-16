package com.introproventures.graphql.jpa.query.example.starwars;

import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaConfigurer;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.model.starwars.Human;
import java.util.HashMap;
import javax.sql.DataSource;
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
public class StarwarsSchemaConfiguration {

    @Bean
    PlatformTransactionManager starWarsTransactionManager() {
        return new JpaTransactionManager(entityManagerFactory().getObject());
    }

    @Bean
    @ConfigurationProperties(prefix = "starwars")
    public DataSource starWarsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public ApplicationRunner starWarsDataSourceInitializer() {
        return args -> {
            ResourceLoader resourceLoader = new DefaultResourceLoader();

            ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
            databasePopulator.addScript(resourceLoader.getResource("starwars.sql"));

            databasePopulator.execute(starWarsDataSource());
        };
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        var properties = new HashMap<String, Object>();
        properties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        properties.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCHEMAS, "true");
        properties.put(AvailableSettings.DIALECT, H2Dialect.class.getName());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "true");
        properties.put(
            AvailableSettings.PHYSICAL_NAMING_STRATEGY,
            "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        );

        var vendorAdapter = new HibernateJpaVendorAdapter();
        var factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(starWarsDataSource());
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        factoryBean.setPackagesToScan(Human.class.getPackage().getName());
        factoryBean.setJpaPropertyMap(properties);

        return factoryBean;
    }

    @Bean
    public SharedEntityManagerBean starWarsEntityManager() {
        SharedEntityManagerBean bean = new SharedEntityManagerBean();
        bean.setEntityManagerFactory(entityManagerFactory().getObject());

        return bean;
    }

    @Bean
    GraphQLSchemaConfigurer starWarsGraphQLJpaQuerySchemaConfigurer() {
        return registry ->
            registry.register(
                new GraphQLJpaSchemaBuilder(starWarsEntityManager().getObject()).name("GraphQLStarWars").build()
            );
    }
}
