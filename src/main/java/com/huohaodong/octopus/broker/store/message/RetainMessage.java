package com.huohaodong.octopus.broker.store.message;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetainMessage {
    // 相比于 Publish 消息，Retain 消息并不需要保存 client id 与 packet id
    // Retain 消息用于响应客户端的 Subscribe 消息
    private String topic;

    private MqttQoS QoS;

    private byte[] payload;
}
