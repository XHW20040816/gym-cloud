package com.gym.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.gym.seckill", "com.gym.common", "com.gym.framework"})
@EnableDiscoveryClient
@EnableFeignClients("com.gym.api")
@MapperScan("com.gym.seckill.mapper")
public class GymSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymSeckillApplication.class, args);
    }
}
