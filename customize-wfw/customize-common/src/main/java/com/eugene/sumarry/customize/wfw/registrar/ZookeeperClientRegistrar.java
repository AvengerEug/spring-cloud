package com.eugene.sumarry.customize.wfw.registrar;

import com.eugene.sumarry.customize.wfw.anno.EnableZookeeperClient;
import com.eugene.sumarry.customize.wfw.distributed.zookeeper.ZookeeperClient;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class ZookeeperClientRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (importingClassMetadata.hasAnnotation(EnableZookeeperClient.class.getName())) {
            GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
            genericBeanDefinition.setBeanClass(ZookeeperClient.class);
            genericBeanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

            registry.registerBeanDefinition("zookeeperClient", genericBeanDefinition);
        }
    }
}
