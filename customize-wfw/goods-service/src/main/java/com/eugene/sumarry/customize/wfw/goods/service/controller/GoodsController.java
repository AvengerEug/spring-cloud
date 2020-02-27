package com.eugene.sumarry.customize.wfw.goods.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/v1/goods")
@RestController
public class GoodsController {


    @GetMapping("/index")
    public Message getGoods() {
        Map<String, Object> map = new HashMap<>();
        map.put("goods", "goods1");

        return Message.ok(map);
    }
}
