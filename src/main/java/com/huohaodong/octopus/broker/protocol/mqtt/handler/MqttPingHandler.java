package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPingHandler implements MqttPacketHandler<MqttMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        ctx.channel().writeAndFlush(MqttPublishMessage.PINGRESP);
    }
}
