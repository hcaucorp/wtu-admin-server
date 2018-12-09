package com.jvmp.vouchershop.system;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.jvmp.vouchershop")
@PropertySource("application.properties")
public class DatabaseConfig {


    @Bean
    public DataSource dataSource(
            @Value(PropertyNames.SPRING_DATASOURCE_URL) String url,
            @Value(PropertyNames.SPRING_DATASOURCE_USERNAME) String username,
            @Value(PropertyNames.SPRING_DATASOURCE_PASSWORD) String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    // configure entityManagerFactory

    // configure transactionManager
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource, Environment env) {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("com.hibernate.query.performance.persistence.model");
        sessionFactory.setHibernateProperties(hibernateProperties(env));

        return sessionFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(FactoryBean<SessionFactory> sessionFactory) throws Exception { // TODO: Really need this?
        final HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory.getObject());
        return transactionManager;
    }


    private final Properties hibernateProperties(Environment env) {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "create");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");

        hibernateProperties.setProperty("hibernate.show_sql", "true");
        hibernateProperties.setProperty("hibernate.format_sql", "true");
        // hibernateProperties.setProperty("hibernate.globally_quoted_identifiers", "true");

        return hibernateProperties;
    }

    // configure additional Hibernate Properties
}
