package com.eugene.sumarry.customize.wfw.user.service.feign;

import com.eugene.sumarry.customize.wfw.model.Message;
import org.springframework.stereotype.Component;

/**
 * 要交由spring管理，猜测spring封装后的hystrix会从spring容器中获取
 */
@Component
public class OrderFeignClientHystrix implements OrderFeignClient {

    @Override
    public Message getOrders() {
        return Message.error("获取订单api维护中，请稍后再试");
    }

    @Override
    public Message getFeignOrdersTimeout() {
        return Message.error("通过feign获取订单超时api维护中，请稍后再试");
    }
}
