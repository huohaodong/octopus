package com.huohaodong.octopus.broker.store.subscription;

import java.util.Set;

public interface SubscriptionMatcher {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String clientId, String topic);

    Set<Subscription> match(String topic);

    int size();

}
