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

## 六、Eureka和zookeeper的区别

* 由于CAP定律的存在，三者不可能同时满足。所以这就是eureka的核心区别所在。

* eureka设计的核心是为了满足A(Avalibility)P(Partition tolerance), 即可用性性和分区容错性

* zookeeper设计的核心是为了满足C(Consistency)P(Partition tolerance), 即一致性和分区容错性。对于zookeeper而言，若集群中的一个节点发生故障或一个节点中的数据发生改变后，zookeeper会将集群中的所有节点都变成不可用状态(为了一致性)。

* 什么时候使用zookeeper搭建集群什么时候使用eureka搭建集群呢？

  ```markdown
  * 主要看业务场景，zookeeper的设计核心是为了满足ap，即一致性。
  1. 什么时候要保证数据一致(CP)呢？(使用zookeeper架构)
  	=> 很明显呀，跟钱有关的东西。比如银行转账, 转账的时候我们必须要确定银行卡里还剩多少钱才能转账成功。
  2. 那什么时候要保证可用性(AP)呢？(使用eureka架构)
  	=> 比如双十一，我们在双十一下单的时候，有时候商品数量显示还剩100件，但是我们下单的时候就提示无货了。这是为什么呢？因为双十一要保证系统的可用性，商品数量的显示其实不影响用户的下单操作(因为用户在抢单，都是在某个时间点去抢购的)，最终的下单成功是要在后台进行数量校验的。所以此时我们要保证可用性，一致性在这个场景下就没什么重要的意义了。除了双十一还有秒杀场景、火车票抢票场景等等。
  	
  `其实什么时候用ap什么时候用cp。重要看业务场景，如果这个业务场景对于数据的一致性要求没那么高，那么就使用AP架构, 保证可用性。如果这个业务场景对于数据的一致性要求很高，那么就使用CP架构架构。`
  ```

  