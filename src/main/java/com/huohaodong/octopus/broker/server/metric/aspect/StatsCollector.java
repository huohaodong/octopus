package com.huohaodong.octopus.broker.server.metric.aspect;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Aspect
@Getter
@Component
public class StatsCollector {

    protected final AtomicLong deltaTotalConnections = new AtomicLong();
    protected final AtomicLong deltaTotalTopics = new AtomicLong();
    protected final AtomicLong deltaTotalSubscriptions = new AtomicLong();
    protected final AtomicLong deltaTotalRetained = new AtomicLong();
    protected final AtomicLong deltaTotalReceived = new AtomicLong();
    protected final AtomicLong deltaTotalSent = new AtomicLong();
    protected final AtomicLong netTrafficReceived = new AtomicLong();
    protected final AtomicLong netTrafficSent = new AtomicLong();
    private final StringRedisTemplate redisTemplate;
    private final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(1);
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_CONNECTION")
    public String TOTAL_CONNECTIONS;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_TOPICS")
    public String TOTAL_TOPICS;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_SUBSCRIPTIONS")
    public String TOTAL_SUBSCRIPTIONS;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_RETAINED")
    public String TOTAL_RETAINED;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_RECEIVED")
    public String TOTAL_RECEIVED;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:TOTAL_SENT")
    public String TOTAL_SENT;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:NET_TRAFFIC_RECEIVED")
    public String NET_TRAFFIC_RECEIVED;
    @Value("STATS:${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:NET_TRAFFIC_SENT")
    public String NET_TRAFFIC_SENT;

    public StatsCollector(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.annotation.ConnectionMetric)")
    public void connectionPointcut() {
        deltaTotalConnections.incrementAndGet();
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.annotation.DisconnectionMetric)")
    public void disconnectionPointcut() {
        deltaTotalConnections.decrementAndGet();
    }

    @AfterReturning(value = "@annotation(com.huohaodong.octopus.broker.server.metric.annotation.RetainMetric)",
            returning = "delta")
    public void retainPointcut(int delta) {
        deltaTotalRetained.accumulateAndGet(delta, Long::sum);
    }

    @AfterReturning(value = "@annotation(com.huohaodong.octopus.broker.server.metric.annotation.UnRetainMetric)",
            returning = "delta")
    public void unRetainPointcut(int delta) {
        deltaTotalRetained.accumulateAndGet(-delta, Long::sum);
    }

    @AfterReturning(value = "@annotation(com.huohaodong.octopus.broker.server.metric.annotation.SentMetric)",
            returning = "sentCount")
    public void sentPointcut(int sentCount) {
        deltaTotalSent.accumulateAndGet(sentCount, Long::sum);
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.annotation.ReceivedMetric)")
    public void receivedPointcut() {
        deltaTotalReceived.incrementAndGet();
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.annotation.SubscribeMetric)")
    public void subscribePointcut() {
        deltaTotalSubscriptions.incrementAndGet();
    }

    @After("@annotation(com.huohaodong.octopus.broker.server.metric.annotation.UnSubscribeMetric)")
    public void unSubscribePointcut() {
        deltaTotalSubscriptions.decrementAndGet();
    }

    @PostConstruct
    public void setupTimer() {
        // TODO 通过配置文件配置 push 间隔
        TIMER.scheduleAtFixedRate(this::publish, 0, 5, TimeUnit.SECONDS);
    }

    @Transactional
    public void publish() {
        redisTemplate.opsForValue().increment(TOTAL_CONNECTIONS, deltaTotalConnections.get());
        redisTemplate.opsForValue().increment(TOTAL_TOPICS, deltaTotalTopics.get());
        redisTemplate.opsForValue().increment(TOTAL_SUBSCRIPTIONS, deltaTotalSubscriptions.get());
        redisTemplate.opsForValue().increment(TOTAL_RETAINED, deltaTotalRetained.get());
        redisTemplate.opsForValue().increment(TOTAL_RECEIVED, deltaTotalReceived.get());
        redisTemplate.opsForValue().increment(TOTAL_SENT, deltaTotalSent.get());
        log.info("Stats: Publish Broker Stats Info to Redis");
        resetDelta();
    }

    public void resetDelta() {
        deltaTotalConnections.set(0);
        deltaTotalTopics.set(0);
        deltaTotalSubscriptions.set(0);
        deltaTotalRetained.set(0);
        deltaTotalReceived.set(0);
        deltaTotalSent.set(0);
    }

}
