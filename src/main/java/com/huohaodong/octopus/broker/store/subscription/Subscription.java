package com.huohaodong.octopus.broker.store.subscription;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Subscription {

    private final String clientId;

    private final String topic;

    private MqttQoS QoS;

}
