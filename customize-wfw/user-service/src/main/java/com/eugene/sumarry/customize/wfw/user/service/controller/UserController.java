package com.eugene.sumarry.customize.wfw.user.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
public class UserController {


    // 服务名，由服务的spring.application.name属性决定，
    // 在注册的时候，会将这个属性的值作为服务名注册到eureka中
    // 在服务间调用时，只需要将服务名添加即可，不需要加端口好
    // 因为集群时，有可能每个实例的端口不一致
    // ribbon是客户端集群，在发送具体请求时，会先从eureka中知道对应方有哪些实例
    // 然后知道具体使用哪一个实例的ip和端口进行访问
    private static final String ORDER_SERVICE_NAME = "ORDER-SERVICE";

    private static final String GOODS_SERVICE_NAME = "GOODS-SERVICE";

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
        return Message.ok(restTemplate.getForObject("http://" + ORDER_SERVICE_NAME +"/v1/orders/index", Object.class));
    }

    @GetMapping("/get-goods")
    public Message getGoods() {
        return Message.ok(restTemplate.getForObject("http://" + GOODS_SERVICE_NAME +"/v1/goods/index", Object.class));

    }
}
