package com.huohaodong.octopus.broker.store.subscription;

import com.huohaodong.octopus.broker.store.subscription.trie.Subscription;

import java.util.Set;

public interface SubscriptionManager {
    Set<Subscription> listAllSubscriptions();

    void addNewSubscription(Subscription subscription);

    void removeSubscription(String topic, String clientID);
}
