package com.huohaodong.octopus.broker.service.subscription.impl;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.common.persistence.entity.Subscription;
import com.huohaodong.octopus.common.persistence.service.subscription.SubscriptionManager;
import com.huohaodong.octopus.common.persistence.service.subscription.SubscriptionMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class InMemorySubscriptionService implements SubscriptionManager {
    /* 每个 Topic 对应的所有符合条件的 Subscription (ClientId 和 Topic) */
    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();

    /*每个 clientId 对应的订阅信息 */
    private final ConcurrentHashMap<String, List<Subscription>> map = new ConcurrentHashMap<>();

    private final BrokerProperties brokerProperties;

    @Override
    public void subscribe(Subscription subscription) {
        matcher.subscribe(subscription);
        map.putIfAbsent(subscription.getClientId(), new CopyOnWriteArrayList<>());
        map.get(subscription.getClientId()).add(subscription);
    }

    @Override
    public void unSubscribe(String brokerId, String clientId, String topic) {
        matcher.unSubscribe(clientId, topic);
        map.get(clientId).removeIf(
                subscription -> subscription.getBrokerId().equals(brokerProperties.getId())
                                && subscription.getClientId().equals(clientId)
                                && subscription.getTopic().equals(topic)
        );
    }

    @Override
    public Collection<Subscription> getAllMatched(String brokerId, String topicFilter) {
        return matcher.match(topicFilter);
    }

    @Override
    public Collection<Subscription> getAllSubscription(String brokerId, String clientId) {
        return map.get(clientId);
    }

    @Override
    public void unSubscribeAll(String brokerId, String clientId) {
        getAllSubscription(brokerId, clientId).forEach(sub -> matcher.unSubscribe(sub.getClientId(), sub.getTopic()));
        map.remove(clientId);
    }
}
