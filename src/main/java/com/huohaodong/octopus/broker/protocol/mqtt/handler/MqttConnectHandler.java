package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttConnectHandler implements MqttPacketHandler<MqttConnectMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttConnectMessage msg) {

    }
}
