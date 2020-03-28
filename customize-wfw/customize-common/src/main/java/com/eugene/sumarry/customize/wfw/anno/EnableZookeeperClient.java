package com.eugene.sumarry.customize.wfw.anno;

import com.eugene.sumarry.customize.wfw.registrar.ZookeeperClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ ZookeeperClientRegistrar.class})
public @interface EnableZookeeperClient {
}
