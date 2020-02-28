package com.eugene.sumarry.customize.wfw.order.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/orders")
public class OrderController {

    @GetMapping("/index")
    public Message getOrders() {
        Map<String, Object> map = new HashMap<>();
        map.put("orders", "order2");

        return Message.ok(map);
    }

    @GetMapping("/get-feign-orders-time-out")
    public Message getFeignOrdersTimeout() {
        try {
            // 模拟休眠5s钟
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Message.ok();
    }
}
