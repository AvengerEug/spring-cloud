package com.eugene.sumarry.customize.wfw.user.service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    /**
     * @LoadBalanced 注解表示此restTemplate使用负载均衡
     * 使用到了ribbon组件，ribbon组件不需要重复依赖，因为eureka已经依赖了他们
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /*@Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        tomcatServletWebServerFactory.setPort(5000);

        return tomcatServletWebServerFactory;
    }*/


}
