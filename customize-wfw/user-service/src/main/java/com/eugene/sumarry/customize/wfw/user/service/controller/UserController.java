package com.eugene.sumarry.customize.wfw.user.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import com.eugene.sumarry.customize.wfw.user.service.feign.OrderFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private AtomicInteger circuitBreakerAtomic = new AtomicInteger(0);

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

    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/index")
    public Message getUsers() {
        Map<String, Object> map = new HashMap<>();
        map.put("users", "users");

        return Message.ok(map);
    }

    /**
     *
     * 设置了超时的属性，若请求超过3s(默认1s)未响应，则使用降级方法进行return
     *
     * 注意: @HystrixProperty注解中的property的key最好是从当前项目依赖的hystrix版本的
     * HystrixCommandProperties.java 文件去找，有可能每个版本的property key不一致
     *
     * 注意: 官网配置文档: https://github.com/Netflix/Hystrix/wiki/Configuration中
     * 每一个配置都是以如下的方式进行展示的。
     * circuitBreaker.enabled  *****------> 这个是配置在注解上的，假如要针对某个方法进行熔断，则需要局部配置******
     * This property determines whether a circuit breaker will be used to track health and to short-circuit requests if it trips.
     *
     * Default Value	true
     * Default Property	hystrix.command.default.circuitBreaker.enabled  ***** ---> 这个配置是配置在yml文件中的，供全局使用。*****
     * Instance Property	hystrix.command.HystrixCommandKey.circuitBreaker.enabled
     * How to Set Instance Default
     * HystrixCommandProperties.Setter()
     *    .withCircuitBreakerEnabled(boolean value)
     *
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "getUsersFallBack",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
            }
    )
    @GetMapping("/index-time-out")
    public Message getUsersTimeout() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Message.ok();
    }

    /**
     * 测试此方法时，在5秒内请求5次，会失败。
     * 那么就会走getUsersFallBack方法。
     *
     * 走了getUsersFallBack方法后。
     * 因为要考虑到服务有可能是因为网络问题没有得到及时响应而打开了断路器的开关，
     * 所以在断路器开关开启后，后面的请求可能会偶尔(这个偶尔可以配置)将请求移动到此方法中，若此方法
     * 是通的，则将返回值响应出去，否则走的还是getUsersFallBack方法的逻辑
     *
     * 因为配置了对6取余。 所以前面5次请求(连续6s内)都不满足条件，最终执行失败。 导致此api被熔断了。
     * 因为熔断后可能会偶尔将请求又发送到api中，所以此时满足了余数等于0的情况，所以浏览器能响应成功
     *
     * 所以最终的情况就是: 在浏览器6s内连续请求http://localhost:5000/v1/users/index-circuit-breaker
     * 前面5次走的都是getUsersFallBack方法。 因为抛异常了，此时是因为方法降级的原因
     * 当执行到第6次的时候，因为被熔断了，所以走得还是getUsersFallBack方法。但其中会偶尔请求到此api，
     * 此时是满足余数等于0的情况，所以浏览器会偶尔出现请求成功
     *
     * @return
     * @throws Exception
     */
    @HystrixCommand(
            fallbackMethod = "getUsersFallBack",
            commandProperties = {
                    // 5秒内
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"),
                    // 连续5次请求没有得到响应(或者抛了异常)，那么断路器将会全开，下一次请求到此api的请求则走getUsersFallBack方法
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5")
            }
    )
    @GetMapping("/index-circuit-breaker")
    public Message getUsersCircuitBreaker() throws Exception {

        if (circuitBreakerAtomic.incrementAndGet() % 6 != 0) {
            throw new Exception();
        }

        return Message.ok();
    }

    public Message getUsersFallBack() {
        return Message.error("系统正在维护中，请稍后再试 -- getUsersFallBack");
    }

    /**
     * 此方法做了hystrix提供的"方法降级"处理，
     * 因为order模块使用的是自定义的负载均衡策略，
     * 当请求到达order1中，order1模块中的controller抛了异常，那么此时就会方法降级，
     * 将方法getFeignOrdersFallBack的返回值 return给调用者来防止系统雪崩
     * @return
     */
    @HystrixCommand(fallbackMethod = "getFeignOrdersFallBack")
    @GetMapping("/get-feign-orders")
    public Message getFeignOrders() {
        return orderFeignClient.getOrders();
    }


    public Message getFeignOrdersFallBack() {
        return Message.error("系统正在维护中，请稍后再试");
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
