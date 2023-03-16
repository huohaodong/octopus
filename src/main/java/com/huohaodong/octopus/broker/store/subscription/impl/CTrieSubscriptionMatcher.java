package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import com.huohaodong.octopus.broker.store.subscription.trie.CTrie;
import com.huohaodong.octopus.broker.store.subscription.trie.Topic;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
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

    @Override
    public int size() {
        return ctrie.size();
    }

    public String dumpTree() {
        return ctrie.dumpTree();
    }
}
