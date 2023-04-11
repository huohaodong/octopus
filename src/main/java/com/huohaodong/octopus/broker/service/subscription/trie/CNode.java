package com.huohaodong.octopus.broker.service.subscription.trie;


import com.huohaodong.octopus.broker.service.subscription.Subscription;

import java.util.*;

public class CNode {

    Set<Subscription> subscriptions;
    private Token token;
    private final List<INode> children;

    CNode() {
        this.children = new ArrayList<>();
        this.subscriptions = new HashSet<>();
    }

    private CNode(Token token, List<INode> children, Set<Subscription> subscriptions) {
        this.token = token;
        this.subscriptions = new HashSet<>(subscriptions);
        this.children = new ArrayList<>(children);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    boolean anyChildrenMatch(Token token) {
        for (INode iNode : children) {
            final CNode child = iNode.mainNode();
            if (child.equalsToken(token)) {
                return true;
            }
        }
        return false;
    }

    List<INode> allChildren() {
        return this.children;
    }

    INode childOf(Token token) {
        for (INode iNode : children) {
            final CNode child = iNode.mainNode();
            if (child.equalsToken(token)) {
                return iNode;
            }
        }
        throw new IllegalArgumentException("Asked for a token that doesn't exists in any child [" + token + "]");
    }

    private boolean equalsToken(Token token) {
        return this.token != null && this.token.equals(token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    CNode copy() {
        return new CNode(this.token, this.children, this.subscriptions);
    }

    public void add(INode newINode) {
        this.children.add(newINode);
    }

    public void remove(INode node) {
        this.children.remove(node);
    }

    CNode addSubscription(Subscription newSubscription) {
        if (subscriptions.contains(newSubscription)) {
            final Subscription existing = subscriptions.stream().filter(s -> s.equals(newSubscription)).findFirst().get();
            subscriptions.remove(existing);
            subscriptions.add(new Subscription(newSubscription.getClientId(), newSubscription.getTopic(), newSubscription.getQoS()));
        } else {
            this.subscriptions.add(new Subscription(newSubscription.getClientId(), newSubscription.getTopic(), newSubscription.getQoS()));
        }
        return this;
    }

    boolean containsOnly(String clientId) {
        for (Subscription sub : this.subscriptions) {
            if (!sub.getClientId().equals(clientId)) {
                return false;
            }
        }
        return !this.subscriptions.isEmpty();
    }

    public boolean contains(String clientId) {
        for (Subscription sub : this.subscriptions) {
            if (sub.getClientId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    void removeSubscriptionsFor(String clientId) {
        Set<Subscription> toRemove = new HashSet<>();
        for (Subscription sub : this.subscriptions) {
            if (sub.getClientId().equals(clientId)) {
                toRemove.add(sub);
            }
        }
        this.subscriptions.removeAll(toRemove);
    }
}
