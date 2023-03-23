package com.huohaodong.octopus.broker.server.metric.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class StatsAspect {
    @After("@annotation(com.huohaodong.octopus.broker.server.metric.aspect.annotation.ConnectionMetric)")
    public void connectionPointcut() {
        System.out.println("connectionPointcut");
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.aspect.annotation.DisconnectionMetric)")
    public void disconnectionPointcut() {
        System.out.println("disconnectionPointcut");
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.aspect.annotation.ConnectionMetric)")
    public void retainPointcut() {
        System.out.println("retainPointcut");
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.aspect.annotation.DisconnectionMetric)")
    public void unRetainPointcut() {
        System.out.println("unRetainPointcut");
    }


}
