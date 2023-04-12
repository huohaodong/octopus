package com.huohaodong.octopus.broker.service.subscription;

import com.huohaodong.octopus.broker.persistence.entity.Subscription;

import java.util.Collection;

public interface SubscriptionMatcher {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String clientId, String topic);

    Collection<Subscription> match(String topic);

}
