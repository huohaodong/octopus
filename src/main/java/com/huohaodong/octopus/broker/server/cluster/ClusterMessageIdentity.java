package com.huohaodong.octopus.broker.server.cluster;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClusterMessageIdentity {

    private int clusterMessageId;

    /* 该消息属于哪个 Broker 组 */
    private String group;

    /* 发送该消息的 Broker Id */
    private String brokerId;

    // TODO 添加消息权限验证的 Token
//    private String token;
}
