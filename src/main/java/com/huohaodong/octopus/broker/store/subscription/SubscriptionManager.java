package com.huohaodong.octopus.broker.store.subscription;

import com.huohaodong.octopus.broker.store.subscription.trie.Subscription;

import java.util.Set;

public interface SubscriptionManager {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic, String clientId);

    Set<Subscription> match(String topic);

    int size();

    String dumpTree();

}
