package com.gym.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(scanBasePackages = {"com.gym.search", "com.gym.common", "com.gym.framework"})
@EnableDiscoveryClient
@MapperScan("com.gym.search.mapper")
@EnableElasticsearchRepositories(basePackages = "com.gym.search.repository")
public class GymSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSearchApplication.class, args);
    }
}
