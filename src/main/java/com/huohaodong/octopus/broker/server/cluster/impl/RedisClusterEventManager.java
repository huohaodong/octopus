package com.huohaodong.octopus.broker.server.cluster.impl;

import com.google.gson.Gson;
import com.huohaodong.octopus.broker.config.BrokerConfig;
import com.huohaodong.octopus.broker.server.cluster.ClusterCloseChannelMessage;
import com.huohaodong.octopus.broker.server.cluster.ClusterEventManager;
import com.huohaodong.octopus.broker.server.cluster.ClusterMessageIdentity;
import com.huohaodong.octopus.broker.server.cluster.ClusterPublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.session.ChannelManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final BrokerConfig brokerConfig;

    private final Gson GSON = new Gson();

    private final int MAX_MESSAGE_ID = 65535;

    private final AtomicInteger CLUSTER_MESSAGE_ID = new AtomicInteger(0);

    private final RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();

    private final StringRedisTemplate redisTemplate;

    private final SubscriptionManager subscriptionManager;

    private final SessionManager sessionManager;

    private final PublishMessageManager publishMessageManager;

    private final ChannelManager channelManager;

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:PUB:BRIDGE")
    private String PUB_BRIDGE_TOPIC;

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:CLOSE:BRIDGE")
    private String CLOSE_BRIDGE_TOPIC;

    public RedisClusterEventManager(StringRedisTemplate stringRedisTemplate, RedisConnectionFactory connectionFactory,
                                    BrokerConfig brokerConfig, SubscriptionManager subscriptionManager,
                                    SessionManager sessionManager, PublishMessageManager publishMessageManager, ChannelManager channelManager) {
        this.brokerConfig = brokerConfig;
        this.redisTemplate = stringRedisTemplate;
        this.publishMessageManager = publishMessageManager;
        this.channelManager = channelManager;
        this.listenerContainer.setConnectionFactory(connectionFactory);
        this.listenerContainer.afterPropertiesSet();
        this.subscriptionManager = subscriptionManager;
        this.sessionManager = sessionManager;
    }

    @PostConstruct
    private void start() {
        listenerContainer.addMessageListener(this, ChannelTopic.of(PUB_BRIDGE_TOPIC));
        listenerContainer.addMessageListener(this, ChannelTopic.of(CLOSE_BRIDGE_TOPIC));
        listenerContainer.start();
        log.info("Cluster is listening on {}", PUB_BRIDGE_TOPIC);
        log.info("Cluster is listening on {}", CLOSE_BRIDGE_TOPIC);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = redisTemplate.getStringSerializer().deserialize(message.getChannel());
        String rawValue = redisTemplate.getStringSerializer().deserialize(message.getBody());
        if (topic.equals(PUB_BRIDGE_TOPIC)) {
            ClusterPublishMessage clusterMessage = GSON.fromJson(rawValue, ClusterPublishMessage.class);
            if (isValid(clusterMessage.getIdentity())) {
                sendPublishMessage(clusterMessage.getPayload().getTopic(), clusterMessage.getPayload().getPayload());
                log.info("Broadcast-Pub: from GROUP: {}, Broker: {}", clusterMessage.getIdentity().getGroup(), clusterMessage.getIdentity().getBrokerId());
            }
        } else if (topic.equals(CLOSE_BRIDGE_TOPIC)) {
            ClusterCloseChannelMessage clusterMessage = GSON.fromJson(rawValue, ClusterCloseChannelMessage.class);
            if (isValid(clusterMessage.getIdentity())) {
                closeChannelByClientId(clusterMessage.getClientToClose());
                log.info("Broadcast-Pub: from GROUP: {}, Broker: {}", clusterMessage.getIdentity().getGroup(), clusterMessage.getIdentity().getBrokerId());
            }
            log.info("Broadcast-Close: from GROUP: {}, Broker: {}", clusterMessage.getIdentity().getGroup(), clusterMessage.getIdentity().getBrokerId());
        } else {
            log.warn("Broadcast-Unknown Event Type");
        }
    }

    @Override
    public void broadcastToPublish(PublishMessage message) {
        ClusterPublishMessage clusterBroadCastMessage = new ClusterPublishMessage(
                ClusterMessageIdentity.of(nextMessageId(), brokerConfig.getGroup(), brokerConfig.getId()),
                message
        );
        redisTemplate.convertAndSend(PUB_BRIDGE_TOPIC, GSON.toJson(clusterBroadCastMessage));
    }

    @Override
    public void broadcastToClose(String clientToClose) {
        ClusterCloseChannelMessage clusterCloseChannelMessage = new ClusterCloseChannelMessage(
                ClusterMessageIdentity.of(nextMessageId(), brokerConfig.getGroup(), brokerConfig.getId()),
                clientToClose
        );
        redisTemplate.convertAndSend(CLOSE_BRIDGE_TOPIC, GSON.toJson(clusterCloseChannelMessage));
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
                    Channel channel = channelManager.getChannel(session.getClientId());
                    if (channel != null) {
                        channel.writeAndFlush(publishMessage);
                    }
                }
            }
        });
    }

    private void closeChannelByClientId(String clientId) {
        Session session = sessionManager.get(clientId);
        System.out.println(session);
        if (session == null) {
            return;
        }
        sessionManager.remove(clientId);
        if (session.isCleanSession()) {
            subscriptionManager.unSubscribeAll(clientId);
            publishMessageManager.removeAllByClientId(clientId);
        }
        Channel channel = channelManager.getChannel(session.getClientId());
        if (channel != null) {
            channel.writeAndFlush(MqttMessage.DISCONNECT).addListener((ChannelFutureListener) future -> channel.close());
        }
    }

    // TODO 添加鉴权逻辑
    private boolean isValid(ClusterMessageIdentity identity) {
        // 消息处理条件：消息属于本 Broker Group 且不是自己发出的消息
        return identity.getGroup().equals(brokerConfig.getGroup())
                && !identity.getBrokerId().equals(brokerConfig.getId());
    }
}
