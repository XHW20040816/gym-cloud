package com.gym.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.gym.user", "com.gym.common", "com.gym.framework"})
@EnableDiscoveryClient
@MapperScan("com.gym.user.mapper")
@EnableFeignClients(basePackages = "com.gym.api")
public class GymUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymUserApplication.class, args);
    }
}
