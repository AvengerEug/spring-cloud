# 自定义实现微服务模块

## 一、如何添加eureka，为自己写的服务添加服务注册中心模块

* 步骤：

  1. 因为本项目的微服务最终会往spring cloud方向去扩展，所以最终使用了spring cloud中提供的eureka组件。

  2. 如何添加依赖？是从spring cloud官网提供的[eureka server demo]()(https://github.com/spring-cloud-samples/configserver) (https://spring.io/projects/spring-cloud 页面中的**Config Server**)中找到了pom文件(Spring Cloud官方文档中没有针对快速搭建spring cloud的文档)。 最终从添加spring-cloud依赖开始着手: 

     ```xml
     <!-- 添加了Finchley.SR2版本的spring-cloud依赖 -->
     <!-- 把它加到dependencyManagement标签中的原因是让子类项目依赖此groupId时不再需要指定版本号 -->
     <dependencyManagement>
         <dependencies>
             <dependency>
                 <groupId>org.springframework.cloud</groupId>
                 <artifactId>spring-cloud-dependencies</artifactId>
                 <version>Finchley.SR2</version>
                 <type>pom</type>
                 <scope>import</scope>
             </dependency>
         </dependencies>
     </dependencyManagement>
     
     <!-- 添加一些仓库地址：(加这些的原因是我在maven仓库中下载不了一些jar包) 是从spring cloud官方文档中的demo中找到的
         https://github.com/spring-cloud-samples/eureka/blob/master/pom.xml -->
     <repositories>
         <repository>
             <id>spring-snapshots</id>
             <name>Spring Snapshots</name>
             <url>https://repo.spring.io/libs-snapshot</url>
             <snapshots>
                 <enabled>true</enabled>
             </snapshots>
         </repository>
         <repository>
             <id>spring-milestones</id>
             <name>Spring Milestones</name>
             <url>https://repo.spring.io/libs-milestone</url>
             <snapshots>
                 <enabled>false</enabled>
             </snapshots>
         </repository>
         <repository>
             <id>spring-releases</id>
             <name>Spring Releases</name>
             <url>https://repo.spring.io/libs-release</url>
             <snapshots>
                 <enabled>false</enabled>
             </snapshots>
         </repository>
     </repositories>
     <pluginRepositories>
         <pluginRepository>
             <id>spring-snapshots</id>
             <name>Spring Snapshots</name>
             <url>https://repo.spring.io/libs-snapshot-local</url>
             <snapshots>
                 <enabled>true</enabled>
             </snapshots>
         </pluginRepository>
         <pluginRepository>
             <id>spring-milestones</id>
             <name>Spring Milestones</name>
             <url>https://repo.spring.io/libs-milestone-local</url>
             <snapshots>
                 <enabled>false</enabled>
             </snapshots>
         </pluginRepository>
     </pluginRepositories>
     ```

  3. 添加Eureka服务端

     * 添加eureka服务端依赖

       ```xml
       <dependencies>
           <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-eureka-server -->
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
           </dependency>
       
       </dependencies>
       ```

     * 入口启动类添加**@EnableEurekaServer**注解

     * yml文件添加如下配置:

       ```yml
       server:
         port: 8000
       
       spring:
         application:
           name: eureka #当前应用程序的名称，最终会在eureka服务器页面中显示实例的名字
       
       ---
       eureka:
         server:
           enable-self-preservation: false # 关闭健康检测
         instance:
           hostname: localhost
         # 添加eureka 客户端服务注册配置, 注册中心暴露这个地址进行注册，客户端就是通过此配置来关联上对应的eureka的
         client:
           service-url:
             # eureka客户端要注册到服务与注册中心的url
             defaultZone: http://${eureka.instance.hostname}:${server.port}/${spring.application.name}
           # 服务端不注册，即当前的eureka不注册到自己身上
           register-with-eureka: false
       ```

  4. 添加Eureka客户端

     * 添加eureka客户端

       ```xml
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       </dependency>
       ```

     * yml文件添加如下配置:

       ```yml
       server:
         port: 6000
       
       spring:
         application:
           name: user-service
       
       eureka:
         client:
           service-url:
             # 在Eureka服务器端配置的url
             defaultZone: http://localhost:8000/eureka
       ```

     * 入口启动类添加@EnableEurekaClient注解即可

  5. 访问: **localhost:8000**即可访问服务注册中心的控制台了



## 二、 Spring Cloud各版本与Spring Boot版本的要求

* | Spring Cloud版本 | Spring Boot版本 |
  | :--------------: | :-------------: |
  |      Hoxton      |      2.2.x      |
  |    Greenwich     |      2.1.x      |
  |     Finchley     |      2.0.x      |
  |     Edgware      |      1.5.x      |
  |     Dalston      |      1.5.x      |

## 三、CAP概念
* CAP原则又称CAP定理，指的是在一个分布式系统中。一致性(Consistency)、可用性(Availability)、分布容错性
  (Partition tolerance). 这三个要素最多只能同时实现两个，不可能三者同时实现

* C(Consistency): 若分布式部署时，有个节点有集群操作。我在集群节点A中对一个内存中的数据做了操作。我想在
  节点B中获取到修改后的数据。此时我必须将在节点A修改数据的操作同步到节点B中。而在同步过程中为了保证不出错，
  我必须暂停对节点B中针对此数据的请求，待同步完成后再开放请求。

* A(Availability): 只要用户发送请求就必须响应数据，

* P(Partition tolerance): 分布式部署时，多台服务器每个模块可能部署在不同机器上，而这些机器可能分布在不
  同的区域(比如机器A在中国北京，机器B在美国洛杉矶)，而他们要通行，难免避免不了网络问题而导致的通信失败。
  
* C(一致性)和A(可用性)的矛盾:  如要保证一致性，那么必然会存在所有服务暂停使用的情况。这与可用性矛盾了。

## 四、使用ribbon组件实现负载均衡
* 使用ribbon组件实现负载均衡需要依赖于RestTemplate对象, 即使用@LoadBalance注解来标识这个bean要使用负载均衡
  具体步骤如下:
  
  1. 使用@LoadBalance标识RestTemplate对象
  
     ```java
     /**
      * @LoadBalanced 注解表示此restTemplate使用负载均衡
      * 使用到了ribbon组件，ribbon组件不需要重复依赖，因为eureka已经依赖了他们
      * @return
      */
     @Bean
     @LoadBalanced
     public RestTemplate restTemplate() {
         return new RestTemplate();
     }
     ```
  
  2. 使用http方式调用另外一个微服务，假设有订单模块且清单模块做了集群，一共有两个实例。且他们的服务名都叫`order-service`.  所以在使用reetTemplate进行http调用时，只需要填写服务名即可，不需要填写端口，如下:
  
     ```java
     http://order-service/v1/get-orders
     ```
  
     具体执行流程为: 在请求order-service服务之前，会先从rureka中得知我具体要用哪一个实例，即得到实例的ip和端口，最终进行具体的调用
  
* ribbon一共有7个负载均衡策略，但是默认使用的是轮询策略。

  具体默认七个负载均衡策略如下:

  1. RoundRobinRule:  轮询策略，默认这种策略(我debug时，没有进这个方法，所以默认是不是这个策略需要确认)
  2. RandomRule: 随机策略服务，
  3. AvailabilityFilteringRule: 会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务、还有并发的链接数量超过阈值的服务，然后对剩余的服务按照轮询策略进行选取
  4. WeightedResponseTimeRule: 根据平均响应时间计算所有服务的权重，响应越快服务权重越大。刚启动时如果统计信息不足，则使用RoundRobinRule(轮询)策略，等统计信息足够后，会切换到WeightedResponseTimeRule
  5. RetryRule: 先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定时间内会重试，再获取不到则放弃
  6. BestAvalableRule:  会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务
  7. ZoneAvoidanceRule: 默认规则，符合判断server所在区域的性能和server的可用性选择服务

* 客户端负载均衡和服务端负载均衡的区别:

  1. 服务端负载均衡是我不知道我具体要请求哪一个实例，由负载均衡代理服务器决定
  2. 客户端负载均衡时我知道具体要请求哪一个实例
  3. 其实就是正向代理和反向代理的区别

## 五、实现自定义负载均衡策略

### 5.1 实现步骤

1. 实现IRule接口, 并重写里面的方法。 它会要求你内部维护一个**ILoadBalancer**类型的对象, 该对象在spring实例化时会将此对象自动装配进去。**ILoadBalancer**类型的对象维护了注册到服务的所有信息。

   接口信息如下:

   ```java
   public interface IRule{
   
       public Server choose(Object key);
       
       public void setLoadBalancer(ILoadBalancer lb);
       
       public ILoadBalancer getLoadBalancer();    
   }
   ```

2. 将负载均衡的逻辑全部写在**choose**方法中，可以根据自己的逻辑来实现决定具体返回哪一个server。

3. 具体实现逻辑参考: [Git项目: https://github.com/AvengerEug/spring-cloud customize-loadbalance分支代码](https://github.com/AvengerEug/spring-cloud)

### 5.2原理

1. 进入类**LoadBalanced**, 查看哪个地方用了这个类(eclipse 快捷键: ctrl + alt + f7), 易发现有类**LoadBalancerAutoConfiguration**, 其中很容易发现，此bean中维护了一个泛型为**RestTemplate**的list，并有@Autowired注解修饰。 所以可知，在这个bean中维护了并值一个list，里面存放了所有的**RestTemplate**。但会自动注入所有持有@LoadBalanced注解的RestTemplate的bean

2. 在此**LoadBalancerAutoConfiguration**类中，会根据项目中存在哪个bean而产生具体的bean。

   * 若项目中缺失这个类**org.springframework.retry.support.RetryTemplate**, 那么将处理这个**LoadBalancerInterceptorConfig**类内部的@Bean方法
   * 若项目中存在这个类**org.springframework.retry.support.RetryTemplate**，那么将处理**RetryAutoConfiguration**和**RetryInterceptorAutoConfiguration**类中的bean。

   根据spring-cloud **Finchley.SR2**版本，处理的是**LoadBalancerInterceptorConfig**类中的bean。最终在restTemplateCustomizer方法中，针对所有的restTemplate添加了一个拦截器**loadBalancerInterceptor**. 

3. 在使用restTemplate发送请求时，首先要获取一个ClientHttpRequest(AbstractClientHttpRequest)， 并执行execute方法。在AbstractClientHttpRequest内部再执行executeInternal方法，此方法是一个抽象方法，最终在子类InterceptingClientHttpRequest中执行，最终再执行内部类InterceptingRequestExecution的execute方法。最终拿到外部类(InterceptingClientHttpRequest)的interceptors，并执行对应的intercept方法(只会执行第一个，因为只获取到了迭代器的next，并没有循环获取)。而这个intercept就是**LoadBalancerInterceptor**, 最终在这个**LoadBalancerInterceptor**的RibbonLoadBalancerClient的execute方法，并根据负载均衡策略拿到具体的服务，并请求具体的服务

4. 那么问题来了: 上述的AbstractClientHttpRequest类中的interceptors拦截器是什么时候被设置进去的呢？同理，在eclispe中按ctrl  + alt + f7来查看使用到它的地方。然后发现它在InterceptingClientHttpRequestFactory类中被手动new的，并将interceptors传进去。所以现在再来看看InterceptingClientHttpRequestFactory类中的interceptors是什么时候被填充进去的呢？看了下InterceptingClientHttpRequestFactory类，发现它有一个带参构造方法，而参数就是interceptors。所以现在看下这个带参构造方法是什么时候被调用的。同理，按ctrl + alt + f7。按照这样的思路，最终发现它就是在**LoadBalancerAutoConfiguration**类中处理RestTemplateCustomizer这个bean时，调用了restTemplate的setInterceptors方法，将所有的拦截器都添加进去了

   ```java
   @Bean
   @ConditionalOnMissingBean
   public RestTemplateCustomizer restTemplateCustomizer(
         final LoadBalancerInterceptor loadBalancerInterceptor) {
      return restTemplate -> {
                 List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                         restTemplate.getInterceptors());
                 list.add(loadBalancerInterceptor);
                 restTemplate.setInterceptors(list);
             };
   }
   ```

   但是，返回的仅仅是一个对象，传入的restTemplate是一个参数。所以我们要定位什么时候调用了RestTemplateCustomizer的customize方法。同上，使用ctrl + alt + f7快捷键。最终发现在**LoadBalancerAutoConfiguration**类的loadBalancedRestTemplateInitializerDeprecated()方法被调用了。但是这个方法返回的也是一个对象

   ```java
   @Bean
   public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
         final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
      return () -> restTemplateCustomizers.ifAvailable(customizers -> {
              for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
                  for (RestTemplateCustomizer customizer : customizers) {
                      customizer.customize(restTemplate);
                  }
              }
          });
   }
   ```

   , 我们还要看什么时候调用了SmartInitializingSingleton接口的afterSingletonsInstantiated方法。熟悉spring源码的人就会知道，此接口会在spring 完成所有bean的创建后，会统一调用SmartInitializingSingleton接口的afterSingletonsInstantiated方法，具体方法如下

   ```java
   // DefaultListableBeanPostProcessor.java
   // preInstantiateSingletons() 方法
   // Trigger post-initialization callback for all applicable beans...
   for (String beanName : beanNames) {
      Object singletonInstance = getSingleton(beanName);
      if (singletonInstance instanceof SmartInitializingSingleton) {
         final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
         if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
               smartSingleton.afterSingletonsInstantiated();
               return null;
            }, getAccessControlContext());
         }
         else {
            smartSingleton.afterSingletonsInstantiated();
         }
      }
   }
   ```

5. 所以最终的大致流程就是: 在springboot启动项目后，加载spring.factories文件，开始处理**org.springframework.boot.autoconfigure.EnableAutoConfiguration**指定的自动装配类，其中包括LoadBalancerAutoConfiguration类，最终在满足创建这个bean的条件时，开始创建这个bean, 最终将加了@LoadBalance注解的RestTemplate的bean注入到此类中的restTemplates属性中。同时，将此类中维护的下述的bean也创建出来。

   ```java
   @Bean
   public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
         final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
      return () -> restTemplateCustomizers.ifAvailable(customizers -> {
              for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
                  for (RestTemplateCustomizer customizer : customizers) {
                      customizer.customize(restTemplate);
                  }
              }
          });
   }
   ```

   ```java
   /**
    LoadBalancerInterceptor类型的参数，spring在调用这个方法时，会自动填充进去
   **/
   @Bean
   @ConditionalOnMissingBean
   public RestTemplateCustomizer restTemplateCustomizer(
         final LoadBalancerInterceptor loadBalancerInterceptor) {
      return restTemplate -> {
                 List<ClientHttpRequestInterceptor> list = new ArrayList<>(
                         restTemplate.getInterceptors());
                 list.add(loadBalancerInterceptor);
                 restTemplate.setInterceptors(list);
             };
   }
   ```

   最终在spring完成所有bean创建后再统一处理SmartInitializingSingleton类型的方法，所以此刻会执行上述第一个方法的返回值的方法，即里面的lamda表达式

   ```java
   () -> restTemplateCustomizers.ifAvailable(customizers -> {
       for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
           for (RestTemplateCustomizer customizer : customizers) {
               customizer.customize(restTemplate);
           }
       }
   })
   ```

   最终在执行到customizer.customize(restTemplate)时，执行的就是上述第二个方法的lamda表达式

   ```java
   restTemplate -> {
       List<ClientHttpRequestInterceptor> list = new ArrayList<>(
       restTemplate.getInterceptors());
       list.add(loadBalancerInterceptor);
       restTemplate.setInterceptors(list);
   }
   ```

   最终完成了将所有的拦截器set到restTemplate中去的功能。