package com.huohaodong.octopus.broker.server.cluster;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
public class ClusterPublishMessage implements Serializable {

    private static final long serialVersionUID = -289323607827073890L;

    private final ClusterMessageIdentity identity;

    private final PublishMessage payload;

    public ClusterPublishMessage(ClusterMessageIdentity identity, PublishMessage payload) {
        this.identity = identity;
        this.payload = payload;
    }

    public static ClusterPublishMessage of(ClusterMessageIdentity identity, PublishMessage payload) {
        return new ClusterPublishMessage(identity, payload);
    }

}
