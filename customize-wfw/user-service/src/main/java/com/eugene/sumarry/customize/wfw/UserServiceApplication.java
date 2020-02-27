package com.eugene.sumarry.customize.wfw;

import com.eugene.sumarry.customize.wfw.user.service.anno.EnableCustomizeLoadBalance;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableCustomizeLoadBalance
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class);
    }
}
