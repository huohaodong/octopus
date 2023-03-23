package com.huohaodong.octopus.broker.server.metric.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TimedRedisStatsPublisher {

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
    protected AtomicLong totalConnections = new AtomicLong();
    protected AtomicLong totalTopics = new AtomicLong();
    protected AtomicLong totalSubscriptions = new AtomicLong();
    protected AtomicLong totalRetained = new AtomicLong();
    protected AtomicLong totalReceived = new AtomicLong();
    protected AtomicLong totalSent = new AtomicLong();
    protected AtomicLong netTrafficReceived = new AtomicLong();
    protected AtomicLong netTrafficSent = new AtomicLong();

    public TimedRedisStatsPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void setupTimer() {
        // TODO 通过配置文件配置 push 间隔
        TIMER.scheduleAtFixedRate(this::publish, 5, 60, TimeUnit.SECONDS);
    }

    @Transactional
    public void publish() {
        redisTemplate.opsForValue().set(TOTAL_CONNECTIONS, totalConnections.toString());
        redisTemplate.opsForValue().set(TOTAL_TOPICS, totalTopics.toString());
        redisTemplate.opsForValue().set(TOTAL_SUBSCRIPTIONS, totalSubscriptions.toString());
        redisTemplate.opsForValue().set(TOTAL_RETAINED, totalRetained.toString());
        redisTemplate.opsForValue().set(TOTAL_RECEIVED, totalReceived.toString());
        redisTemplate.opsForValue().set(TOTAL_SENT, totalSent.toString());
        log.info("Stats: Publish Broker Stats Info to Redis");
    }
}
