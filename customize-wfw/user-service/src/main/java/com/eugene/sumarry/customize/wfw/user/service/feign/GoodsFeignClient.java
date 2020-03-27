package com.eugene.sumarry.customize.wfw.user.service.feign;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("GOODS-SERVICE")
@RequestMapping("/v1/goods")
public interface GoodsFeignClient {

    @GetMapping("/index")
    Message getGoods();
}
