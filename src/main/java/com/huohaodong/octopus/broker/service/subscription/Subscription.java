package com.huohaodong.octopus.broker.service.subscription;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

import java.io.Serializable;

@Data
public class Subscription implements Serializable {

    private static final long serialVersionUID = 4809548486583011883L;

    private String clientId;

    private String topic;

    private MqttQoS QoS;

    public Subscription() {
    }

    public Subscription(String clientId, String topic) {
        this.clientId = clientId;
        this.topic = topic;
    }

    public Subscription(String clientId, String topic, MqttQoS QoS) {
        this.clientId = clientId;
        this.topic = topic;
        this.QoS = QoS;
    }
}
