package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttUnSubscribeHandler implements MqttPacketHandler<MqttUnsubscribeMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {

    }
}
