package com.gym.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ConfigurationPropertiesScan("com.gym.gateway.config")
public class GymGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymGatewayApplication.class, args);
    }
}
