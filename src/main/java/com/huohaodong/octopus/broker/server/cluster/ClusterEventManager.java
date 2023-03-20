package com.huohaodong.octopus.broker.server.cluster;

import com.huohaodong.octopus.broker.store.message.PublishMessage;

public interface ClusterEventManager {

    void broadcastToPublish(PublishMessage message);

    void broadcastToClose(String clientToClose);

}
