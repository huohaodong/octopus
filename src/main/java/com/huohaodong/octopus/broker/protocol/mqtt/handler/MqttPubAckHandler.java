package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttPubAckHandler implements MqttPacketHandler<MqttPubAckMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPubAckMessage msg) {

    }
}
