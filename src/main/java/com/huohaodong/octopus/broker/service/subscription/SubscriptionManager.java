package com.huohaodong.octopus.broker.service.subscription;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.springframework.lang.Nullable;

import java.util.Collection;

/*
 *  订阅关系管理器
 */
public interface SubscriptionManager {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String clientId, String topicFilter);

    @Nullable
    Collection<Subscription> getAllMatched(String topicFilter);

    @Nullable
    Collection<Subscription> getAllByClientId(String clientId);

    void unSubscribeAll(String clientId);

    long size();

    default boolean subscribe(String clientId, String topic) {
        return subscribe(new Subscription(clientId, topic));
    }

    default boolean subscribe(String clientId, String topic, MqttQoS QoS) {
        return subscribe(new Subscription(clientId, topic, QoS));
    }

    default boolean unSubscribe(Subscription subscription) {
        return unSubscribe(subscription.getClientId(), subscription.getTopic());
    }

}
