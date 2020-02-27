package com.eugene.sumarry.customize.wfw;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class GoodsApplication2 {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication2.class);
    }
}
