package com.eugene.sumarry.customize.wfw.user.service.controller;

import com.eugene.sumarry.customize.wfw.model.Message;
import com.eugene.sumarry.customize.wfw.user.service.feign.GoodsFeignClient;
import com.eugene.sumarry.customize.wfw.user.service.feign.OrderFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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

    @Autowired
    private GoodsFeignClient goodsFeignClient;

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
     * Default Property	hystrix.command.default.circuitBreaker.enabled  ***** ---> 这个配置是配置在yml文件中的，供 全局使用。*****
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
        int sleepSecond = new Random().nextInt(5);

        logger.info("Sleep time is {} second", sleepSecond);
        try {
            Thread.sleep(sleepSecond * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Message.ok();
    }


    /**
     * 设置这个api限流，若有2个线程进来了，后面的线程则进入降级方法
     * 若线程使用数未达到2，那么则继续进入此api
     *
     * 具体配置参考此模块的application.yml
     *
     * hystrix.threadpool.limiting.coreSize
     * 其中配置中的limiting就和下面 threadPoolKey = "limiting"相对应，
     * hystrix.command.limiting.execution.isolation.thread.timeoutInMilliseconds
     * 和下面的commandKey = "limiting"相对应
     *
     * 最终会
     * 从配置文件中来填充
     *
     * @return
     */
    @HystrixCommand(
            fallbackMethod = "getUsersFallBack",
            commandKey = "limiting",
            threadPoolKey = "limiting"
    )
    @GetMapping("/index-limiting")
    public Message getUsersLimiting() {
        logger.info("Requesting /index-limiting api");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Message.ok();
    }

    /**
     * 测试断路器:
     *
     * 测试此方法时，在5秒内请求5次，全失败，此时断路器就会打开(半开状态)
     * 那么就会走getUsersFallBack方法。
     *
     * 当一直刷新到circuitBreakerAtomic > 10时，且达到了断路器偶尔将请求分配到
     * 此方法的条件时，api就会返回正常了。
     *
     * 走了getUsersFallBack方法后。
     * 因为要考虑到服务有可能是因为网络问题没有得到及时响应而打开了断路器的开关，
     * 所以在断路器开关开启后，后面的请求可能会偶尔(这个'偶尔'可以配置)将请求移动到此方法中，若此方法
     * 是通的，则将返回值响应出去，否则走的还是getUsersFallBack方法的逻辑
     *
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

        circuitBreakerAtomic.incrementAndGet();

        logger.info("circuitBreakerAtomic value is {}", circuitBreakerAtomic);

        if (circuitBreakerAtomic.get() < 10) {
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

    /**
     * HystrixCommand默认是超过一秒就采取方法降级的策略
     * 因为orderFeignClient配置了fallback属性，所以走的是实现了OrderFeignClient接口的类(降级类)
     * 的方法
     *
     * 因为配置了超时属性为3s
     *
     * 而因为集群的原因，order实例线程是睡眠了2s
     * order2实例线程是睡眠了5秒。
     * 因为自定义负载均衡的原因。若请求到order2实例，那么就会走降级的方法
     * 若是请求到order1实例，那么走的就是正常逻辑，只不过要等2秒后才会响应
     *
     * 但这里会出现一个问题, 若请求到order2实例，因为hystrix中对应ORDER-SERVICE模块
     * 的feign的getFeignOrdersTimeout方法设置的超时时间5s
     * 而order2实例睡眠了5s。而ribbon默认设置的重试时间为1秒，1秒 < 10秒
     * 所以ribbon会重新调用api进行重试，导致负载均衡实例算法被调用了两次，
     * 如果再发一次请求就又到了order1实例中去了
     *
     * 所以现在想将ribbon的重试机制给去掉，
     * 看了下官网的描述:
     * https://cloud.spring.io/spring-cloud-static/Finchley.SR4/single/spring-cloud.html#retrying-failed-requests
     * 大概就是从两个方面下手:
     * 1. 配置spring.cloud.loadbalancer.retry.enabled=false
     * 2. 去除spring-retry依赖
     *
     * 第一个方面这么做了，可是无效
     *   -> 原因是spring cloud 2.x以后的版本此配置都无效了
     * 第二个方面还没找到到底是哪个模块依赖了spring-retry模块。无法使用exclusions标签进行移除
     *   -> 待确认
     *
     * 最终的解办法是，将ribbon api重试机制的时间设置成比hystrix还要大，这样的话就相当于重试机制失效了
     * 假设hystrix设置的超时时间为10s，
     * 假设ribbon设置的重试时间为5s。那么在5s到达后，ribbon认为此api不需要走降级方法。所以它会进行重试
     * (重新请求一次。)
     * 假设riibon设置的重试时间为11s。那么ribbon永远不会做重试操作，因为在10s的时候，hystrix已经走了
     * 降级方法了。
     *
     *
     *
     * @return
     */
    @GetMapping("/get-feign-orders-time-out")
    public Message getFeignOrdersTimeout() {
        return orderFeignClient.getFeignOrdersTimeout();
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

    @GetMapping("/get-feign-goods")
    public Message getFeignGoods() {
        return Message.ok(goodsFeignClient.getGoods());
    }

}
