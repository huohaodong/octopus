package com.huohaodong.octopus.broker.store.message;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

@Data
public class PublishMessage {
    // PUBLISH 消息中并不包含 clientId 信息，因此需要自己保存，比如放到对应的 channel 里面当成一个 Attribute 处理
    private String clientId;

    private int messageId;

    private String topic;

    private MqttQoS QoS;

    private byte[] payload;

    private MqttMessageType type;
}
