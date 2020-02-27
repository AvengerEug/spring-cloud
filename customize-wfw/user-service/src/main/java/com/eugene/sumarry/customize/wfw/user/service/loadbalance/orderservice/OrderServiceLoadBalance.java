package com.eugene.sumarry.customize.wfw.user.service.loadbalance.orderservice;

import com.eugene.sumarry.customize.wfw.user.service.loadbalance.SecondRuleForLoadBalance;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderServiceLoadBalance {

    @Bean
    public IRule orderServiceLoadBalanceRule() {
        return new SecondRuleForLoadBalance();
    }

}
