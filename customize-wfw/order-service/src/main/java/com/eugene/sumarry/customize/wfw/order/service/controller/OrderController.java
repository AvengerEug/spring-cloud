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
    public Message getOrders() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("orders", "order1");
        String value = null;

        if (value == null) {
            throw new Exception();
        }

        return Message.ok(map);
    }


    @GetMapping("/get-feign-orders-time-out")
    public Message getFeignOrdersTimeout() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Message.ok();
    }
}
