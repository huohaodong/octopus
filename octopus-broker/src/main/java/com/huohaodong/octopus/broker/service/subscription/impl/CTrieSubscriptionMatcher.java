package com.huohaodong.octopus.broker.service.subscription.impl;

import com.huohaodong.octopus.common.persistence.entity.Subscription;
import com.huohaodong.octopus.common.persistence.service.subscription.SubscriptionMatcher;
import com.huohaodong.octopus.broker.service.subscription.trie.CTrie;
import com.huohaodong.octopus.broker.service.subscription.trie.Topic;

import java.util.Set;

public class CTrieSubscriptionMatcher implements SubscriptionMatcher {

    private final CTrie ctrie;

    public CTrieSubscriptionMatcher() {
        this.ctrie = new CTrie();
    }

    @Override
    public Set<Subscription> match(String topicFilter) {
        Topic topic = new Topic(topicFilter);
        return ctrie.recursiveMatch(topic);
    }

    @Override
    public boolean subscribe(Subscription newSubscription) {
        Set<Subscription> matched = match(newSubscription.getTopic());
        matched.forEach(subscription -> {
            if (subscription.getTopic().equals(newSubscription.getTopic())
                && subscription.getClientId().equals(newSubscription.getClientId())) {
                unSubscribe(subscription.getClientId(), subscription.getTopic());
            }
        });
        ctrie.addToTree(newSubscription);
        return true;
    }

    @Override
    public boolean unSubscribe(String clientID, String topicFilter) {
        ctrie.removeFromTree(new Topic(topicFilter), clientID);
        return true;
    }
}
