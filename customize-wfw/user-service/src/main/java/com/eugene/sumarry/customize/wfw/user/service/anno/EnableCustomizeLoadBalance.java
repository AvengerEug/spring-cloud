package com.eugene.sumarry.customize.wfw.user.service.anno;

import com.eugene.sumarry.customize.wfw.constants.Constants;
import com.eugene.sumarry.customize.wfw.user.service.loadbalance.orderservice.OrderServiceLoadBalance;
import com.eugene.sumarry.customize.wfw.user.service.loadbalance.userservice.UserServiceLoadBalance;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = Constants.USER_SERVICE_EXCLUDE_PACKAGE)
})
@RibbonClients({
        @RibbonClient(name = "ORDER-SERVICE", configuration = OrderServiceLoadBalance.class),
        @RibbonClient(name = "USER-SERVICE", configuration = UserServiceLoadBalance.class)
})
public @interface EnableCustomizeLoadBalance {
}
