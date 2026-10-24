package com.gym.coach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.gym.coach", "com.gym.common", "com.gym.framework"})
@EnableDiscoveryClient
@EnableFeignClients("com.gym.api")
@MapperScan("com.gym.coach.mapper")
public class GymCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCoachApplication.class, args);
    }
}
