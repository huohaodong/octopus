package com.huohaodong.octopus.broker.service.subscription.trie;

import java.util.concurrent.atomic.AtomicInteger;

class SubscriptionCounterVisitor implements CTrie.IVisitor<Integer> {

    private final AtomicInteger accumulator = new AtomicInteger(0);

    @Override
    public void visit(CNode node, int deep) {
        accumulator.addAndGet(node.subscriptions.size());
    }

    @Override
    public Integer getResult() {
        return accumulator.get();
    }
}
