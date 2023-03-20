package com.huohaodong.octopus.broker.server.cluster.impl;

import com.huohaodong.octopus.broker.server.cluster.ClusterEventManager;
import com.huohaodong.octopus.broker.server.cluster.ClusterMessageIdentity;
import io.netty.handler.codec.mqtt.MqttMessage;

public class RedisClusterEventManager implements ClusterEventManager {
    @Override
    public void broadcast(ClusterMessageIdentity identity, MqttMessage message) {

    }

    @Override
    public void closeByClientId(ClusterMessageIdentity identity, String ClientId) {

    }
}
