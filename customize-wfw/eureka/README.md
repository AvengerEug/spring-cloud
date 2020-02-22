# Spring Cloud Eureka原理

## 一、Eureka是什么
* Eureka是Netﬂix的一个模块，它的定位和nginx类似，是一个中间层服务器。可以用于服务注册与发现
  以及故障转移。但它比nginx更强大。把nginx作为中间层服务器做负载均衡时，若其中一个实例挂了，
  nginx无法检测到。但eureka可以检测到，这是它的其中一个优点。
  
## 二、它和zookeeper的区别
* .....

## 三、@EnableEurekaServer注解执行原理
* 该注解只做了一件事，就是初始化了一个bean: **EurekaServerMarkerConfiguration**。 有什么用呢？它创建了EurekaServerMarkerConfiguration的内部类Marker。对于EurekaServerMarkerConfiguration的内部类Marker，它的作用是来决定是否自动装配**EurekaServerAutoConfiguration**类。在**EurekaServerAutoConfiguration**类中添加了**@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)**的注解，即当EurekaServerMarkerConfiguration.Marker.class这个bean存在时，spring才会处理**EurekaServerAutoConfiguration**类。最终来自动配置eureka。
* 上面是利用了springboot的@Condition系列的注解，其实可以使用@Import + ImportBeanDefinitionRegistrar类来实现这个功能，即来判断传入类是否存在@EnableEurekaClient注解即可。

## 四、Eureka的保护机制
* 当注册到eureka的服务，15分钟内超过85%的服务器都没心跳后，eureka才认为有可能是自己的原因。因为这种机制的存在，所以我们手动停掉一个
  服务的话，并没有达到85%，所以eureka会认为只是那个停掉的服务可能因为网络问题而没有发送心跳。此时在eureka的dashbord中还是能看到停掉
  的服务。

## 五、Eureka集群配置

* 服务端配置

  ```yml
  eureka:
    server:
      enable-self-preservation: false # 关闭自我保护机制
      eviction-interval-timer-in-ms: 3000 # 设置服务的清理间隔(若有服务3s内没有发送心跳给eureka，则会移除它)，(单位毫秒: 默认是60 * 1000)
    instance:
      # 服务实例的名称
      hostname: localhost
  
    # 添加eureka 客户端服务注册配置, 注册中心暴露这个地址进行注册，客户端就是通过此配置来关联上对应的eureka的
    client:
      service-url:
        defaultZone: http://eureka8001.com:8001/eureka, http://eureka8002.com:8002/eureka
      # 服务端不注册，即当前的eureka不注册到自己身上
      register-with-eureka: false
  ```

* 注释: 总共有三个eureka，其中保持：defaultZone中不填写自己的配置的原则来配置高可用eureka集群配置。保证只要有一个eureka都能正常工作。且eureka的集群能共享数据的，即我在eureka实例A中注册了服务，同时在eureka其他实例中也能看到

* 客户端配置

  ```yml
  eureka:
    client:
      service-url:
        defaultZone: http://eureka8000.com:8000/eureka,http://eureka8001.com:8001/eureka,http://eureka8002.com:8002/eureka
  ```

* 注释: 填写三个eureka实例的defaultZone地址，这样能保证高可用，就算其中任何一个eureka宕机都能保证服务正常的注册。其中配置的三个eureka注册地址，只要有一个注册上了，剩下的地址则不会再去注册(因为eureka集群是共享的)