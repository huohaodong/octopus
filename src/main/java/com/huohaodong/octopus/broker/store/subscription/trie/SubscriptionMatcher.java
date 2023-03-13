package com.huohaodong.octopus.broker.store.subscription.trie;

import java.util.Set;

/**
 * Subscription tree
 */
public interface SubscriptionMatcher {

    /**
     * add subscribe
     *
     * @return true：new subscribe,dispatcher retain message
     * false：no need to dispatcher retain message
     */
    boolean subscribe(Subscription subscription);

    boolean unSubscribe(String topic, String clientId);

    Set<Subscription> match(String topic);

    int size();

    String dumpTree();

}
