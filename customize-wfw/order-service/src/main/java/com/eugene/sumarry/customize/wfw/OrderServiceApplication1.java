package com.eugene.sumarry.customize.wfw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class OrderServiceApplication1 {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication1.class);
    }
}
