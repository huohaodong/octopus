package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.persistent.Repository;
import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;

import java.util.Set;

public class InMemorySubscriptionManager implements SubscriptionManager {

    private final SubscriptionMatcher matcher;

    private final Repository<String, Subscription> repository;

    public InMemorySubscriptionManager(SubscriptionMatcher matcher, Repository<String, Subscription> repository) {
        this.matcher = matcher;
        this.repository = repository;
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        repository.put(subscription.getClientId(), subscription);
        return matcher.subscribe(subscription);
    }

    @Override
    public boolean unSubscribe(String clientId, String topicFilter) {
        repository.remove(clientId);
        return matcher.unSubscribe(clientId, topicFilter);
    }

    @Override
    public Set<Subscription> getAllMatched(String topicFilter) {
        return matcher.match(topicFilter);
    }
}
