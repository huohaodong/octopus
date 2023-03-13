package com.huohaodong.octopus.broker.store.subscription.trie;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 订阅关系
 */
@Data
@AllArgsConstructor
public class Subscription {
    private String clientId;
    private String topic;
    private MqttQoS QoS;
}
