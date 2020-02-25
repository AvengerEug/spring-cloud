package com.eugene.sumarry.customize.wfw.user.service.loadbalance;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SecondRuleForLoadBalance implements IRule {

    private ILoadBalancer iLoadBalancer;

    private Vector<Server> resetServers;

    private ConcurrentHashMap<Server, AtomicInteger> serverCalledInfos;

    {
        buildRestServers();
        buildServerCalledInfos();
    }

    private void buildServerCalledInfos() {
        serverCalledInfos = new ConcurrentHashMap<>();
    }

    private void buildRestServers() {
        resetServers = new Vector<>();
    }

    private void initResetServers() {
        this.buildRestServers();
    }

    private void initServerCalledInfos() {
        ILoadBalancer loadBalancer = getLoadBalancer();
        List<Server> allServers = loadBalancer.getAllServers();

        // 为所有的server初始化被调用的数据次数
        for (Server server : allServers) {
            serverCalledInfos.put(server, new AtomicInteger(0));
        }
    }


    private Server choose(ILoadBalancer loadBalancer, Object key) {

        // 在此处进行重新构建集合，因为服务的数量是动态变化的
        if (loadBalancer.getAllServers().size() != serverCalledInfos.size()) {
            // 重置存储被调用服务信息集合
            initServerCalledInfos();
            // 重置存储重置处理服务集合
            initResetServers();
        }

        Server server = null;

        Iterator<Map.Entry<Server, AtomicInteger>> serverCalledInfoIterator = serverCalledInfos.entrySet().iterator();
        while (serverCalledInfoIterator.hasNext()) {
            Map.Entry<Server, AtomicInteger> map = serverCalledInfoIterator.next();

            if (resetServers.contains(map.getKey())) {
                continue;
            }

            if (map.getValue().incrementAndGet() > 2) {
                resetCalledNum(serverCalledInfos, map.getKey());
                resetServers.add(map.getKey());
                continue;
            }

            server = map.getKey();

            break;

        }

        // 如果重置过的server 集合长度和被调用的服务数量一致，那么所有服务的轮询都结束了，重新调用
        if (resetServers.size() == serverCalledInfos.size()) {
            resetServers.clear();
            return choose(loadBalancer, key);
        }

        return server;
    }

    private void resetCalledNum(ConcurrentHashMap<Server, AtomicInteger> serverCalledInfos, Server server) {
        serverCalledInfos.put(server, new AtomicInteger(0));
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void setLoadBalancer(ILoadBalancer iLoadBalancer) {
        this.iLoadBalancer = iLoadBalancer;
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return iLoadBalancer;
    }

}
