package com.eugene.sumarry.customize.wfw.user.service.loadbalance;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SecondRuleForLoadBalance implements IRule {

    private Logger logger = LoggerFactory.getLogger(SecondRuleForLoadBalance.class);

    private Server preServer;

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

        if (serverCalledInfos.size() != 0) {
            serverCalledInfos.clear();
        }

        // 为所有的server初始化被调用的数据次数
        for (Server server : allServers) {
            serverCalledInfos.put(server, new AtomicInteger(0));
        }
    }

    private void resetServerCalledInfos() {
        this.initServerCalledInfos();
    }

    private void resetResetServers() {
        this.initResetServers();
    }

    private boolean isPreServerLoadBalance(ILoadBalancer loadBalancer) {
        return loadBalancer.getAllServers().contains(preServer);
    }


    private Server choose(ILoadBalancer loadBalancer, Object key) {

        if (preServer == null || !isPreServerLoadBalance(loadBalancer)) {
            // 重置当前服务对应的服务信息
            resetServerCalledInfos();
            resetResetServers();
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
            resetResetServers();
            return choose(loadBalancer, key);
        }

        preServer = server;

        logger.info("Current loadBalance service's name is {} and port is {}", server.getHost(), server.getPort());

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
