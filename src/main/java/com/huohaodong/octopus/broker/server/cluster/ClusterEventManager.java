package com.huohaodong.octopus.broker.server.cluster;

import com.huohaodong.octopus.broker.store.message.PublishMessage;

public interface ClusterEventManager {

    void broadcast(PublishMessage message);

    void closeByClientId(ClusterMessageIdentity identity, String ClientId);

}
