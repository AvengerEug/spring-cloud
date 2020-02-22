package com.eugene.sumarry.customize.wfw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class Eureka8002Application {

    public static void main(String[] args) {
        SpringApplication.run(Eureka8002Application.class, args);
    }
}
