package com.jvmp.vouchershop.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by Hubert Czerpak on 2018-12-08
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.jvmp.vouchershop.repository"})
public class DatabaseConfig {
}
