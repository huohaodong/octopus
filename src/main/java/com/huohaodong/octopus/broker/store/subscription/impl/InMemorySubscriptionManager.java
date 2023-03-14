package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.persistent.Repository;
import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;

import java.util.Set;

public class InMemorySubscriptionManager implements SubscriptionManager {

    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();

    private final Repository<String, Subscription> repo = new InMemoryRepository<>();

    @Override
    public boolean subscribe(Subscription subscription) {
        repo.put(subscription.getClientId(), subscription);
        return matcher.subscribe(subscription);
    }

    @Override
    public boolean unSubscribe(String clientId, String topicFilter) {
        repo.remove(clientId);
        return matcher.unSubscribe(clientId, topicFilter);
    }

    @Override
    public Set<Subscription> getAllMatched(String topicFilter) {
        return matcher.match(topicFilter);
    }
}
