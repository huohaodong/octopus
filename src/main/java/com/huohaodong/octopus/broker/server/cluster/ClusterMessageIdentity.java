package com.huohaodong.octopus.broker.server.cluster;

public class ClusterMessageIdentity {

    private final int clusterMessageId;

    /* 该消息属于哪个 Broker 组 */
    private final String group;

    /* 发送该消息的 Broker Id */
    private final String brokerId;

    public ClusterMessageIdentity(int clusterMessageId, String group, String brokerId) {
        this.clusterMessageId = clusterMessageId;
        this.group = group;
        this.brokerId = brokerId;
    }

    public static ClusterMessageIdentity of(int messageId, String group, String brokerId) {
        return new ClusterMessageIdentity(messageId, group, brokerId);
    }

    public int getClusterMessageId() {
        return clusterMessageId;
    }

    public String getGroup() {
        return group;
    }

    public String getBrokerId() {
        return brokerId;
    }
}
