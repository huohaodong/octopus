package com.huohaodong.octopus.broker.store.subscription;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.Set;

/*
 *  订阅关系管理器
 */
public interface SubscriptionManager {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String clientId, String topicFilter);

    Set<Subscription> getAllMatched(String topicFilter);

    default boolean subscribe(String clientId, String topic) {
        return subscribe(new Subscription(clientId, topic));
    }

    default boolean subscribe(String clientId, String topic, MqttQoS QoS) {
        return subscribe(new Subscription(clientId, topic, QoS));
    }

    default boolean unSubscribe(Subscription subscription) {
        return unSubscribe(subscription.getClientId(), subscription.getTopic());
    }

    default int unSubscribeAllMatched(String clientId, String topicFilter) {
        Set<Subscription> matched = getAllMatched(topicFilter);
        int count = 0;
        for (Subscription sub : matched) {
            if (sub.getClientId().equals(clientId)) {
                if (unSubscribe(clientId, topicFilter)) {
                    count++;
                }
            }
        }
        return count;
    }

}
