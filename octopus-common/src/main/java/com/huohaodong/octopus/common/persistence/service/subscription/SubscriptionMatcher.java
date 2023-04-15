package com.huohaodong.octopus.common.persistence.service.subscription;

import com.huohaodong.octopus.common.persistence.entity.Subscription;

import java.util.Collection;

public interface SubscriptionMatcher {

    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String clientId, String topic);

    Collection<Subscription> match(String topic);

}
