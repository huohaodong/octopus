package com.huohaodong.octopus.broker.server.cluster;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
public class ClusterCloseChannelMessage implements Serializable {

    private static final long serialVersionUID = 4621754098457098730L;

    private final ClusterMessageIdentity identity;

    private final String clientToClose;

    public ClusterCloseChannelMessage(ClusterMessageIdentity identity, String clientToClose) {
        this.identity = identity;
        this.clientToClose = clientToClose;
    }

    public static ClusterCloseChannelMessage of(ClusterMessageIdentity identity, String clientToClose) {
        return new ClusterCloseChannelMessage(identity, clientToClose);
    }
}