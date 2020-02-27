# Spring Cloud 学习模块

## 前言
### 几个问题
* **Q:什么叫微服务?**
* **A:以前单体项目是将所有的默认都写在一起，比如一个项目中存在user、order、goods模块。此时若
  系统中查看goods详情的api挂了，最终这个api一直被访问，可能会导致这个项目宕机。最终这个项目都
  会挂掉。或者在项目开发的时候，若我只修改了用户登录功能的地方。我们需要将整个项目重启才能测试
  这个功能。微服务的概念就是将user、order、goods模块给`分离`, 它们可以使用自己的数据库，它们
  之间的交互可以采用一定的协议来交互，比如:http或rpc**
  
---
* **Q:什么叫分布式?**
* **A:分布式与微服务很像。也是将一些模块抽出去，非单体项目。但分布式架构主要解决的问题是并发量
  过高导致服务器处理不过来。所以分布式一般是将不同的模块部署在不同的服务器上**
  
---
* **Q:微服务与分布式的区别**
* **A:基本上没有什么区别，基本上都是对模块的抽离。微服务的核心是将模块分离开来。而分布式的核心是
  将项目中的模块部署在不同机器上来达到缓解服务器压力的功能，若一个单体项目部署在一台机器上，而这台机
  器可能承受不了这么大的并发量所以就会将这个单体项目升级成微服务部署，若服务器还是胜任不了压力，那么
  就会考虑分布式部署了。总而言之，分布式是微服务的一种，而微服务可以部署在一台机器上**

---
* **Q:微服务遇到的问题及解决方案**
* **A:
  1. 微服务是将模块分离出来，若项目比较大，承受压力比较大，最终会做集群，此时需要做负载均衡 ----> ngxin, ribbon可以解决 
  2. 微服务调用链中，若一个服务有问题，为了不让一个服务影响整个业务逻辑，则还需要对有问题的服务做熔断处理 ---> hystrix
  3. 服务间暴露的端口比较多，为了统一管理、安全、服务间路由的定义和过滤，则需要添加网关 ---> zuul能处理
  4. 服务间如何针对某个服务的宕机而忽略它，即服务注册与发现监听 --> eureka或zookeeper也能完成
  **

## 一、自定义微服务模块 customize-wfw
### 1.1 地址

* [https://github.com/AvengerEug/spring-cloud/tree/develop/customize-wfw](https://github.com/AvengerEug/spring-cloud/tree/develop/customize-wfw)

### 1.2 各服务端口汇总

|       服务名        |   端口    |
| :-----------------: | :-------: |
|       eureka        |   8000    |
|    user-service     |   5000    |
| order-service(集群) | 6000/6001 |
| goods-service(集群) | 7000/7001 |

### 1.3 每个服务实现自己的负载均衡策略功能实现步骤

* 背景:

  ```
  假设有两个微服务，分别为order和goods。并且它们都集群部署了。 现在要在user服务中调用order和goods服务的api，要实现对order服务实现自定义的负载均衡算法: `每个实例被连续调用两次后再轮询到另外一个实例`。goods服务要使用普通的轮询算法: `即每个服务调用一次后就轮到其他实例`
  ```

* 实现步骤:

  1. 新建两个类，里面分别维护了两个**IRule**类型的对象。eg，如下:

     ```java
     // OrderServiceLoadBalance.java
     @Configuration
     public class OrderServiceLoadBalance {
     
         @Bean
         public IRule orderServiceLoadBalanceRule() {
             return new SecondRuleForLoadBalance();
         }
     
     }
     ```

      

     ```java
     // UserServiceLoadBalance.java
     @Configuration
     public class UserServiceLoadBalance {
     
         @Bean
         public IRule userServiceLoadBalanceRule() {
             return new RoundRobinRule();
         }
     }
     ```

     `注意事项: `

     ```markdown
     1. 维护负载均衡对象的类必须是一个`@Configuration`注解标识的类
     2. 此对象不能被springboot项目扫描得到。即不能在springboot启动类所在包及其子包下。若扫描到的话，在进行微服务调用时会抛异常(大致的异常就是IRule对象会被其他对象依赖，会根据类型自动注入，但是因为被扫描到了，所以有多个相同类型的对象，spring不知道注入哪一个，所以抛了异常)
     ```

  2. 添加如下自定义注解:

     ```java
     @Target(ElementType.TYPE)
     @Retention(RetentionPolicy.RUNTIME)
     @ComponentScan(excludeFilters = {
             @ComponentScan.Filter(type = FilterType.REGEX, pattern = Constants.USER_SERVICE_EXCLUDE_PACKAGE)
     })
     @RibbonClients({
             @RibbonClient(name = "ORDER-SERVICE", configuration = OrderServiceLoadBalance.class),
             @RibbonClient(name = "USER-SERVICE", configuration = UserServiceLoadBalance.class)
     })
     public @interface EnableCustomizeLoadBalance {
     }
     ```

     注意事项: 

     ```markdown
     1. Constants.USER_SERVICE_EXCLUDE_PACKAGE的值就是手写loadBalance算法类以及上述维护两个@Configureation注解标识的类所在的包
     2. 这个注解的@ComponentScan注解将Constants.USER_SERVICE_EXCLUDE_PACKAGE所在包下的所有类给剔除了，不会被扫描到。
     3. @RibbonClients注解表示具体维护要调用的负载均衡策略。如上，服务名叫"ORDER-SERVICE"的服务名的负载均衡算法使用OrderServiceLoadBalance类中维护的IRule类型的对象，其中ORDER-SERVICE这个值就是order模块中注册到eureka的服务名，就是order模块中配置文件为spring.application.name的值
     ```

  3. 启动自定义负载均衡算法功能, 在user模块中添加@EnableCustomizeLoadBalance注解，如下:

     ```java
     @EnableCustomizeLoadBalance
     @SpringBootApplication
     @EnableEurekaClient
     public class UserServiceApplication {
     
         public static void main(String[] args) {
             SpringApplication.run(UserServiceApplication.class);
         }
     }
     ```

     注意事项: 

     ```markdown
     1. @EnableCustomizeLoadBalance注解必须要写在@SpringBootApplication注解上面。若写在下面，则@EnableCustomizeLoadBalance注解中的过滤扫描包的功能会不起作用。则会抛上述所说的异常。具体原因待研究。
     2. 若单独将@EnableCustomizeLoadBalance注解中的@ComponentScan和@RibbonClients注解内容直接搬到UserServiceApplication类中，那么久不会出现上述所说的异常，具体原因待研究。
     ```

  4. 至此，不同微服务使用不同的负载均衡算法的功能点完成。可以使用如下url进行测试:

     ```tex
     1. http://localhost:5000/v1/users/get-goods  -> user服务调用goods服务api。
        结果: 一次显示goods1。另外一次显示goods。然后轮询显示 => 用的是默认轮询算法
     2. http://localhost:5000/v1/users/get-orders  -> user服务调用order服务api
        结果: 两次显示goods2。两次显示goods1。然后轮询显示  => 用的是自定义的轮询算法
     ```

  5. 最后，因为spring cloud中具有负载均衡功能的组件是ribbon，而ribbon的负载均衡架构是cs模式。所以我们做的负载均衡策略都是写在cs端。就当前demo而言，因为user要调用order和goods服务的api，所以user就相当于是client端。所以要将这些配置加在user这个模块中

  6. [参考官网url:https://cloud.spring.io/spring-cloud-static/Finchley.SR4/single/spring-cloud.html#_customizing_the_ribbon_client](https://cloud.spring.io/spring-cloud-static/Finchley.SR4/single/spring-cloud.html#_customizing_the_ribbon_client)
  
* 1.4 使用feign组件代替restTemplate来进行服务间调用

  1. 什么是feign?

     ```text
     Feign是一个声明式webservice的客户端。spring对这个组件进行了封装，使用方式和spring mvc的api接口定义方式类似。它可以与eureka、ribbon结合，与它们集成后feign能使用ribbon的负载均衡策略
     ```

  2. feign能干什么？

     ```reStructuredText
     spring cloud中微服务的调用方式是采用http的形式来执行的。所以我们需要编写http请求方面的代码。当微服务之间交互比较多时，就需要编写很多的重复代码。而feign类似于mybatis的接口，不需要实现类，只需要接口即可。底层使用代理的技术将http请求操作给封装了。我们只需要按照spring修改后的规则进行编写，就能实现api的调用
     ```

  3. 如何使用？

     1. 添加feign依赖(不同版本的spring cloud可能会出现依赖包artifactid不一致问题，具体可参考官网, Finchley.SR2版本依赖的feign的artifactid为)

        ```xml
        <dependency>
        	<groupId>org.springframework.cloud</groupId>
        	<artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>    
        ```

     2. 在项目入口处添加@EnableFeignClients注解

     3. 编写接口，实现对具体服务的请求api。eg，user模块中编写要调用order模块的client

        ```java
        @FeignClient("ORDER-SERVICE")
        public interface OrderFeignClient {
        
        
            @GetMapping("/v1/orders/index")
            Message getOrders();
        }
        ```

     4. 调用接口

        从spring容器中获取到orderFeignClient这个bean，或者自己自动装配到一个controller中。最终直接调用这个bean的getOrders方法即可。 feign会发送 http://ORDER-SERVICE/v1/orders/index 这个api。最终请求到order模块对应的api。eg:

        ```java
        @RestController
        @RequestMapping("/v1/users")
        public class UserController {
        	@Autowired
            private OrderFeignClient orderFeignClient;
            
            @GetMapping("/get-feign-orders")
            public Message getFeignOrders() {
                return orderFeignClient.getOrders();
            }
            
        }
        ```