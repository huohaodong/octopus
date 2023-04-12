package com.huohaodong.octopus.broker.service.subscription;

import com.huohaodong.octopus.broker.persistence.entity.Subscription;

import java.util.Collection;

public interface SubscriptionManager {
    void subscribe(Subscription subscription);

    void unSubscribe(String brokerId, String clientId, String topic);

    Collection<Subscription> getAllMatched(String brokerId, String topicFilter);

    Collection<Subscription> getAllSubscription(String brokerId, String clientId);

    void unSubscribeAll(String brokerId, String clientId);
}
