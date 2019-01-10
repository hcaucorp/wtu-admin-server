package com.jvmp.vouchershop.system.security;

import com.auth0.AuthenticationController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] NOT_AUTHENTICATED_ENDPOINTS = {
            "/callback",
            "/login",
            "/vouchers/redeem",
            "/shopify/webhook/fulfill"
    };
    @Value(value = "${com.auth0.domain}")
    private String domain;
    @Value(value = "${com.auth0.clientId}")
    private String clientId;
    @Value(value = "${com.auth0.clientSecret}")
    private String clientSecret;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .antMatchers(NOT_AUTHENTICATED_ENDPOINTS).permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .logout().permitAll();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    @Bean
    public AuthenticationController authenticationController() {
        return AuthenticationController.newBuilder(domain, clientId, clientSecret)
                .build();
    }
}
