package com.huohaodong.octopus.broker.store.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Session implements Serializable {
    private static final long serialVersionUID = -7793137701810309284L;

    private String clientId;

    private transient Channel channel;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;
}