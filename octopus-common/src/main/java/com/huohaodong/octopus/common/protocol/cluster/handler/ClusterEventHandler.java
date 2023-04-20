package com.huohaodong.octopus.common.protocol.cluster.handler;

import com.huohaodong.octopus.common.persistence.entity.PublishMessage;

public interface ClusterEventHandler {
    void doPublish(PublishMessage publishMessage);

    void doCloseChannel(String clientId);
}
