package com.huohaodong.octopus.broker.service.subscription.trie;

import com.huohaodong.octopus.common.persistence.entity.Subscription;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CTrie {

    private static final Token ROOT = new Token("root");
    private static final INode NO_PARENT = null;
    INode root;

    public CTrie() {
        final CNode mainNode = new CNode();
        mainNode.setToken(ROOT);
        this.root = new INode(mainNode);
    }

    private NavigationAction evaluate(Topic topic, CNode cnode) {
        if (Token.MULTI.equals(cnode.getToken())) {
            return NavigationAction.MATCH;
        }
        if (topic.isEmpty()) {
            return NavigationAction.STOP;
        }
        final Token token = topic.headToken();
        if (!(Token.SINGLE.equals(cnode.getToken()) || cnode.getToken().equals(token) || ROOT.equals(cnode.getToken()))) {
            return NavigationAction.STOP;
        }
        return NavigationAction.GODEEP;
    }

    public Set<Subscription> recursiveMatch(Topic topic) {
        return recursiveMatch(topic, this.root);
    }

    private Set<Subscription> recursiveMatch(Topic topic, INode inode) {
        CNode cnode = inode.mainNode();
        if (cnode instanceof TNode) {
            return Collections.emptySet();
        }
        NavigationAction action = evaluate(topic, cnode);
        if (action == NavigationAction.MATCH) {
            return cnode.subscriptions;
        }
        if (action == NavigationAction.STOP) {
            return Collections.emptySet();
        }
        Topic remainingTopic = (ROOT.equals(cnode.getToken())) ? topic : topic.exceptHeadToken();
        Set<Subscription> subscriptions = new HashSet<>();
        if (remainingTopic.isEmpty()) {
            subscriptions.addAll(cnode.subscriptions);
        }
        for (INode subInode : cnode.allChildren()) {
            subscriptions.addAll(recursiveMatch(remainingTopic, subInode));
        }
        return subscriptions;
    }

    public void addToTree(Subscription newSubscription) {
        Action res;
        do {
            res = insert(new Topic(newSubscription.getTopic()), this.root, newSubscription);
        } while (res == Action.REPEAT);
    }

    private Action insert(Topic topic, final INode inode, Subscription newSubscription) {
        Token token = topic.headToken();
        if (!topic.isEmpty() && inode.mainNode().anyChildrenMatch(token)) {
            Topic remainingTopic = topic.exceptHeadToken();
            INode nextInode = inode.mainNode().childOf(token);
            return insert(remainingTopic, nextInode, newSubscription);
        } else {
            if (topic.isEmpty()) {
                return insertSubscription(inode, newSubscription);
            } else {
                return createNodeAndInsertSubscription(topic, inode, newSubscription);
            }
        }
    }

    private Action insertSubscription(INode inode, Subscription newSubscription) {
        CNode cnode = inode.mainNode();
        CNode updatedCnode = cnode.copy().addSubscription(newSubscription);
        if (inode.compareAndSet(cnode, updatedCnode)) {
            return Action.OK;
        } else {
            return Action.REPEAT;
        }
    }

    private Action createNodeAndInsertSubscription(Topic topic, INode inode, Subscription newSubscription) {
        INode newInode = createPathRec(topic, newSubscription);
        CNode cnode = inode.mainNode();
        CNode updatedCnode = cnode.copy();
        updatedCnode.add(newInode);

        return inode.compareAndSet(cnode, updatedCnode) ? Action.OK : Action.REPEAT;
    }

    private INode createPathRec(Topic topic, Subscription newSubscription) {
        Topic remainingTopic = topic.exceptHeadToken();
        if (!remainingTopic.isEmpty()) {
            INode inode = createPathRec(remainingTopic, newSubscription);
            CNode cnode = new CNode();
            cnode.setToken(topic.headToken());
            cnode.add(inode);
            return new INode(cnode);
        } else {
            return createLeafNodes(topic.headToken(), newSubscription);
        }
    }

    private INode createLeafNodes(Token token, Subscription newSubscription) {
        CNode newLeafCnode = new CNode();
        newLeafCnode.setToken(token);
        newLeafCnode.addSubscription(newSubscription);

        return new INode(newLeafCnode);
    }

    public void removeFromTree(Topic topic, String clientID) {
        Action res;
        do {
            res = remove(clientID, topic, this.root, NO_PARENT);
        } while (res == Action.REPEAT);
    }

    private Action remove(String clientId, Topic topic, INode inode, INode iParent) {
        Token token = topic.headToken();
        if (!topic.isEmpty() && (inode.mainNode().anyChildrenMatch(token))) {
            Topic remainingTopic = topic.exceptHeadToken();
            INode nextInode = inode.mainNode().childOf(token);
            return remove(clientId, remainingTopic, nextInode, inode);
        } else {
            final CNode cnode = inode.mainNode();
            if (cnode instanceof TNode) {
                return Action.OK;
            }
            if (cnode.containsOnly(clientId) && topic.isEmpty() && cnode.allChildren().isEmpty()) {
                if (inode == this.root) {
                    return inode.compareAndSet(cnode, inode.mainNode().copy()) ? Action.OK : Action.REPEAT;
                }
                TNode tnode = new TNode();
                return inode.compareAndSet(cnode, tnode) ? cleanTomb(inode, iParent) : Action.REPEAT;
            } else if (cnode.contains(clientId) && topic.isEmpty()) {
                CNode updatedCnode = cnode.copy();
                updatedCnode.removeSubscriptionsFor(clientId);
                return inode.compareAndSet(cnode, updatedCnode) ? Action.OK : Action.REPEAT;
            } else {
                return Action.OK;
            }
        }
    }

    private Action cleanTomb(INode inode, INode iParent) {
        CNode updatedCnode = iParent.mainNode().copy();
        updatedCnode.remove(inode);
        return iParent.compareAndSet(iParent.mainNode(), updatedCnode) ? Action.OK : Action.REPEAT;
    }

    private enum Action {
        OK, REPEAT
    }

    private enum NavigationAction {
        MATCH, GODEEP, STOP
    }
}
