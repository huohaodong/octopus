package com.huohaodong.octopus.broker.store.subscription.impl;

import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import com.huohaodong.octopus.broker.store.subscription.trie.CTrie;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.trie.Topic;

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
        ctrie.addToTree(newSubscription);
        return true;
    }

    @Override
    public boolean unSubscribe(String topicFilter, String clientID) {
        ctrie.removeFromTree(new Topic(topicFilter), clientID);
        return true;
    }

    @Override
    public int size() {
        return ctrie.size();
    }

    @Override
    public String dumpTree() {
        return ctrie.dumpTree();
    }
}
