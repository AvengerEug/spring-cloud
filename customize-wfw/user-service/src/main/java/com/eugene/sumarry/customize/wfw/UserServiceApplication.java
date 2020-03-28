package com.eugene.sumarry.customize.wfw;

import com.eugene.sumarry.customize.wfw.anno.EnableZookeeperClient;
import com.eugene.sumarry.customize.wfw.user.service.anno.EnableCustomizeLoadBalance;
import com.eugene.sumarry.customize.wfw.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCustomizeLoadBalance
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableHystrix
@EnableZookeeperClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class);
    }
}
