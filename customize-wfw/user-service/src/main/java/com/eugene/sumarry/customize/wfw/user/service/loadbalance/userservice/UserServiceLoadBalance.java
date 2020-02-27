package com.eugene.sumarry.customize.wfw.user.service.loadbalance.userservice;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserServiceLoadBalance {

    @Bean
    public IRule userServiceLoadBalanceRule() {
        return new RoundRobinRule();
    }
}
