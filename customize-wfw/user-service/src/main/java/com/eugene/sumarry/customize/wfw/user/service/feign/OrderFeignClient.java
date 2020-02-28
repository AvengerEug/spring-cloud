package com.eugene.sumarry.customize.wfw.user.service.feign;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("ORDER-SERVICE")
public interface OrderFeignClient {


    @GetMapping("/v1/orders/index")
    Message getOrders();

    @GetMapping("/v1/orders/get-feign-orders-time-out")
    Message getFeignOrdersTimeout();
}
