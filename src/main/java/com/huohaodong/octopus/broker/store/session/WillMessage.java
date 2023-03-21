package com.huohaodong.octopus.broker.store.session;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class WillMessage implements Serializable {

    private static final long serialVersionUID = 6369359005645512726L;

    private String topic;

    private MqttQoS QoS;

    private byte[] payload;

    private boolean retain;

}
