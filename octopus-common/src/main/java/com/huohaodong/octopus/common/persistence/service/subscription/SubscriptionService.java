package com.huohaodong.octopus.common.persistence.service.subscription;

import com.huohaodong.octopus.common.persistence.entity.Subscription;

import java.util.Collection;

public interface SubscriptionService {
    void subscribe(Subscription subscription);

    void unSubscribe(String brokerId, String clientId, String topic);

    Collection<Subscription> getAllMatched(String brokerId, String topicFilter);

    Collection<Subscription> getAllSubscription(String brokerId, String clientId);

    void unSubscribeAll(String brokerId, String clientId);
}
