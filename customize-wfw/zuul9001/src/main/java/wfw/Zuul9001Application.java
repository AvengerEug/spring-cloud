package wfw;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * zuul 也是eureka的一个客户端，但是不需要添加@EnableEurekaClient注解，
 * 它默认会与eureka进行整合
 */
@SpringBootApplication
@EnableZuulProxy
public class Zuul9001Application {

    public static void main(String[] args) {
        SpringApplication.run(Zuul9001Application.class);
    }
}
