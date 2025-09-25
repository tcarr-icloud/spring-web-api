package com.example.springwebapi;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ApplicationDataSource {
    @Bean
    DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/spring-authentication");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("password");
        return dataSourceBuilder.build();
    }

}
