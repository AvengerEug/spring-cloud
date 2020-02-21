# Spring Cloud Eureka原理

## 一、@EnableEurekaServer注解执行原理
* 该注解只做了一件事，就是初始化了一个bean: **EurekaServerMarkerConfiguration**。 有什么用呢？它创建了EurekaServerMarkerConfiguration的内部类Marker。对于EurekaServerMarkerConfiguration的内部类Marker，它的作用是来决定是否自动装配**EurekaServerAutoConfiguration**类。在**EurekaServerAutoConfiguration**类中添加了**@ConditionalOnBean(EurekaServerMarkerConfiguration.Marker.class)**的注解，即当EurekaServerMarkerConfiguration.Marker.class这个bean存在时，spring才会处理**EurekaServerAutoConfiguration**类。最终来自动配置eureka。
* 上面是利用了springboot的@Condition系列的注解，其实可以使用@Import + ImportBeanDefinitionRegistrar类来实现这个功能，即来判断传入类是否存在@EnableEurekaClient注解即可。