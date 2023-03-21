package com.huohaodong.octopus.broker.store.session;

import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Session implements Serializable {

    private static final long serialVersionUID = -8367142202405231885L;

    private String group;

    private String brokerId;

    private String clientId;

    private ChannelId channelId;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;

}