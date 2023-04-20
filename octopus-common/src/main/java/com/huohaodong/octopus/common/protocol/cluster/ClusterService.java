package com.huohaodong.octopus.common.protocol.cluster;

import com.huohaodong.octopus.common.persistence.entity.PublishMessage;

public interface ClusterService {
    void broadcastPublishMessage(PublishMessage publishMessage);

    void broadcastToClose(String clientId);
}
