package com.huohaodong.octopus.broker.server.cluster.impl;

import com.google.gson.Gson;
import com.huohaodong.octopus.broker.config.BrokerConfig;
import com.huohaodong.octopus.broker.protocol.mqtt.handler.MqttPublishHandler;
import com.huohaodong.octopus.broker.server.cluster.ClusterEventManager;
import com.huohaodong.octopus.broker.server.cluster.ClusterMessageIdentity;
import com.huohaodong.octopus.broker.server.cluster.ClusterPublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RedisClusterEventManager implements MessageListener, ClusterEventManager {
    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:PUB:BRIDGE")
    private String BRIDGE_TOPIC;

    private final Gson GSON = new Gson();

    private final int MAX_MESSAGE_ID = 65535;

    private final AtomicInteger CLUSTER_MESSAGE_ID = new AtomicInteger(0);

    RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();

    StringRedisTemplate redisTemplate;

    SubscriptionManager subscriptionManager;

    SessionManager sessionManager;

    private final BrokerConfig brokerConfig;

    public RedisClusterEventManager(StringRedisTemplate stringRedisTemplate, RedisConnectionFactory connectionFactory,
                                    BrokerConfig brokerConfig, SubscriptionManager subscriptionManager,
                                    SessionManager sessionManager) {
        this.brokerConfig = brokerConfig;
        this.redisTemplate = stringRedisTemplate;
        this.listenerContainer.setConnectionFactory(connectionFactory);
        this.listenerContainer.afterPropertiesSet();
        this.subscriptionManager = subscriptionManager;
        this.sessionManager = sessionManager;
    }

    @PostConstruct
    private void start() {
        listenerContainer.addMessageListener(this, ChannelTopic.of(BRIDGE_TOPIC));
        listenerContainer.start();
        log.info("Cluster is listening on {}", BRIDGE_TOPIC);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        ClusterPublishMessage clusterMessage = GSON.fromJson(((DefaultMessage) message).toString(), ClusterPublishMessage.class);
        // 消息转发条件：消息属于本 Broker Group 且不是自己发出的消息
        if (clusterMessage.getIdentity().getGroup().equals(brokerConfig.getGroup())
                && !clusterMessage.getIdentity().getBrokerId().equals(brokerConfig.getId())) {
                sendPublishMessage(clusterMessage.getPayload().getTopic(), clusterMessage.getPayload().getPayload());
            log.info("Broadcast: from GROUP: {}, Broker: {}", clusterMessage.getIdentity().getGroup(), clusterMessage.getIdentity().getBrokerId());
        }
    }

    @Override
    public void broadcast(PublishMessage message) {
        ClusterPublishMessage clusterBroadCastMessage = new ClusterPublishMessage(
                ClusterMessageIdentity.of(nextMessageId(), brokerConfig.getGroup(), brokerConfig.getId()),
                message
        );
        redisTemplate.convertAndSend(BRIDGE_TOPIC, GSON.toJson(clusterBroadCastMessage));
    }

    @Override
    public void closeByClientId(ClusterMessageIdentity identity, String ClientId) {
        //TODO 根据 SessionManager处理
    }

    private int nextMessageId() {
        return CLUSTER_MESSAGE_ID.accumulateAndGet(1, (left, right) -> {
            int sum = left + right;
            if (sum >= MAX_MESSAGE_ID) {
                return 0;
            }
            return sum % MAX_MESSAGE_ID;
        });
    }

    private void sendPublishMessage(String topic, byte[] payload) {
        Collection<Subscription> subscriptions = subscriptionManager.getAllMatched(topic);
        if (subscriptions == null) {
            return;
        }
        subscriptions.forEach(subscription -> {
            if (sessionManager.contains(subscription.getClientId())) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(payload));
                Session session = sessionManager.get(subscription.getClientId());
                if (session != null) {
                    session.getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }

}
