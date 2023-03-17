package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ConditionalOnProperty(value = "spring.octopus.broker.storage.subscription", havingValue = "local", matchIfMissing = true)
public class InMemorySubscriptionManager implements SubscriptionManager {

    /* 每个 Topic 对应的所有符合条件的 Subscription (ClientId 和 Topic) */
    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();

    /*每个 clientId 对应的订阅信息 */
    private final ConcurrentHashMap<String, List<Subscription>> map = new ConcurrentHashMap<>();

    @Override
    public boolean subscribe(Subscription subscription) {
        if (subscription == null) {
            return false;
        }
        String clientId = subscription.getClientId();
        map.putIfAbsent(clientId, new CopyOnWriteArrayList<>());
        map.get(clientId).add(subscription);
        return matcher.subscribe(subscription);
    }

    @Override
    public boolean unSubscribe(String clientId, String topicFilter) {
        if (!map.containsKey(clientId)) {
            return false;
        }
        map.get(clientId).removeIf(subscription ->
                subscription.getClientId().equals(clientId)
                        && subscription.getTopic().equals(topicFilter)
        );
        if (map.get(clientId).size() == 0) {
            map.remove(clientId);
        }
        return matcher.unSubscribe(clientId, topicFilter);
    }

    @Override
    public Collection<Subscription> getAllMatched(String topicFilter) {
        return matcher.match(topicFilter);
    }

    @Override
    public Collection<Subscription> getAllByClientId(String clientId) {
        return map.get(clientId);
    }

    @Override
    public void unSubscribeAll(String clientId) {
        Collection<Subscription> subscriptions = getAllByClientId(clientId);
        if (subscriptions != null) {
            for (Subscription sub : subscriptions) {
                unSubscribe(sub);
            }
            map.remove(clientId);
        }
    }

}
