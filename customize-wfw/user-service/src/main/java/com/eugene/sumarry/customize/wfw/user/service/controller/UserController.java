package com.eugene.sumarry.customize.wfw.user.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
public class UserController {


    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/index")
    public Message getUsers() {
        Map<String, Object> map = new HashMap<>();
        map.put("users", "users");

        return Message.ok(map);
    }


    @GetMapping("/get-orders")
    public Message getOrders() {
        return Message.ok(restTemplate.getForObject("http://localhost:6000/v1/orders/index", Object.class));
    }
}
