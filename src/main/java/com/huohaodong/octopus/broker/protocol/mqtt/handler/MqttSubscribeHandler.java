package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscribeHandler implements MqttPacketHandler<MqttSubscribeMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {

    }
}
