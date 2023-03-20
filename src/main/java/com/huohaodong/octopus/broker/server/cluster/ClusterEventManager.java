package com.huohaodong.octopus.broker.server.cluster;

import io.netty.handler.codec.mqtt.MqttMessage;

public interface ClusterEventManager {

    void broadcast(ClusterMessageIdentity identity, MqttMessage message);

    void closeByClientId(ClusterMessageIdentity identity, String ClientId);

}
