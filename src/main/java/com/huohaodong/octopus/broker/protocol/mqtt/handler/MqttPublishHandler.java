package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttPublishHandler implements MqttPacketHandler<MqttPublishMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPublishMessage msg) {

    }
}
