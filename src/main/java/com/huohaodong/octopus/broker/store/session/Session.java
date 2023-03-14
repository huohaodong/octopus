package com.huohaodong.octopus.broker.store.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Session {
    private String clientId;

    private Channel channel;

    private boolean cleanSession;

    private MqttMessage willMessage;
}