package com.huohaodong.octopus.broker.service.subscription.trie;


import com.huohaodong.octopus.broker.service.subscription.Subscription;

class TNode extends CNode {

    @Override
    public Token getToken() {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    public void setToken(Token token) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    INode childOf(Token token) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    CNode copy() {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    public void add(INode newINode) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    CNode addSubscription(Subscription newSubscription) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    boolean containsOnly(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    public boolean contains(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    void removeSubscriptionsFor(String clientId) {
        throw new IllegalStateException("Can't be invoked on TNode");
    }

    @Override
    boolean anyChildrenMatch(Token token) {
        return false;
    }
}
