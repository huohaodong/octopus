package com.huohaodong.octopus.broker.service.subscription.impl;

import com.huohaodong.octopus.broker.persistence.entity.Subscription;
import com.huohaodong.octopus.broker.persistence.repository.SubscriptionRepository;
import com.huohaodong.octopus.broker.service.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.service.subscription.SubscriptionMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements SubscriptionManager {
    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();

    /*每个 clientId 对应的订阅信息 */
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void subscribe(Subscription subscription) {
        matcher.subscribe(subscription);
        Optional<Subscription> oldSubscription =
                subscriptionRepository.findByBrokerIdAndClientIdAndTopic(subscription.getBrokerId(),
                        subscription.getClientId(),
                        subscription.getTopic());
        oldSubscription.ifPresent(sub -> subscription.setId(sub.getId()));
        subscriptionRepository.save(subscription);
    }

    @Override
    public void unSubscribe(String brokerId, String clientId, String topic) {
        matcher.unSubscribe(clientId, topic);
        subscriptionRepository.deleteByBrokerIdAndClientIdAndTopic(brokerId, clientId, topic);
    }

    @Override
    public Collection<Subscription> getAllMatched(String brokerId, String topicFilter) {
        return matcher.match(topicFilter);
    }

    @Override
    public Collection<Subscription> getAllSubscription(String brokerId, String clientId) {
        return subscriptionRepository.findAllByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public void unSubscribeAll(String brokerId, String clientId) {
        getAllSubscription(brokerId, clientId).forEach(sub -> matcher.unSubscribe(sub.getClientId(), sub.getTopic()));
        subscriptionRepository.deleteAllByBrokerIdAndClientId(brokerId, clientId);
    }
}
