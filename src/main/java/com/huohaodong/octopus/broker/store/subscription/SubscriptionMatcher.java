package com.huohaodong.octopus.broker.store.subscription;

import java.util.Set;

public interface SubscriptionMatcher {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic, String clientId);

    Set<Subscription> match(String topic);

    int size();

    String dumpTree();

}
