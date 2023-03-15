package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.persistent.Repository;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InMemorySubscriptionManager implements SubscriptionManager {

    /* 每个 Topic 对应的所有符合条件的 Subscription (ClientId 和 Topic) */
    private final SubscriptionMatcher matcher;

    /*每个 clientId 对应的订阅信息 */
    private final Repository<String, List<Subscription>> inMemorySubscriptionRepository;

    public InMemorySubscriptionManager(SubscriptionMatcher matcher, Repository<String, List<Subscription>> inMemorySubscriptionRepository) {
        this.matcher = matcher;
        this.inMemorySubscriptionRepository = inMemorySubscriptionRepository;
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        String clientId = subscription.getClientId();
        if (!inMemorySubscriptionRepository.containsKey(clientId)) {
            inMemorySubscriptionRepository.put(clientId, new CopyOnWriteArrayList<>());
        }
        inMemorySubscriptionRepository.get(clientId).add(subscription);
        return matcher.subscribe(subscription);
    }

    @Override
    public boolean unSubscribe(String clientId, String topicFilter) {
        if (!inMemorySubscriptionRepository.containsKey(clientId)) {
            return false;
        }
        inMemorySubscriptionRepository.get(clientId).removeIf(subscription ->
                subscription.getClientId().equals(clientId)
                        && subscription.getTopic().equals(topicFilter)
        );
        if (inMemorySubscriptionRepository.get(clientId).size() == 0) {
            inMemorySubscriptionRepository.remove(clientId);
        }
        return matcher.unSubscribe(clientId, topicFilter);
    }

    @Override
    public Collection<Subscription> getAllMatched(String topicFilter) {
        return matcher.match(topicFilter);
    }

    @Override
    public Collection<Subscription> getAllByClientId(String clientId) {
        return inMemorySubscriptionRepository.get(clientId);
    }

    @Override
    public int unSubscribeAll(String clientId) {
        Collection<Subscription> subscriptions = getAllByClientId(clientId);
        if (subscriptions == null) {
            return 0;
        }
        for (Subscription sub : subscriptions) {
            unSubscribe(sub);
        }
        inMemorySubscriptionRepository.remove(clientId);
        return subscriptions.size();
    }

}
