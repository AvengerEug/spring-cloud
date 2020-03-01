package com.eugene.sumarry.customize.wfw.user.service.feign;

import com.eugene.sumarry.customize.wfw.model.Message;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * OrderFeignClientHystrix类为实现了OrderFeignClient
 * 接口的方法。若服务提供者满足了hystrix默认的保护微服务机制则会执行降级的方法。
 * 最终执行OrderFeignClientHystrix类对应的方法
 *
 * Hystrix默认的保护微服务机制如下:
 * 1. 请求响应时间超过1s则走降级方法  -- 请求超时
 * 2. 默认5秒内有20个请求没有响应则走降级方法  -- 断路器(熔断)
 * 3. 一个api默认超过10个请求没有响应则走降级方法  -- 限流
 * 等等等等
 *
 */
@FeignClient(value = "ORDER-SERVICE", fallback = OrderFeignClientHystrix.class)
public interface OrderFeignClient {


    @GetMapping("/v1/orders/index")
    Message getOrders();

    @GetMapping("/v1/orders/get-feign-orders-time-out")
    Message getFeignOrdersTimeout();

}
