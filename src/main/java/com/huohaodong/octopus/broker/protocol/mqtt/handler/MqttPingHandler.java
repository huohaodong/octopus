package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPingHandler implements MqttPacketHandler<MqttMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessage pingRespMessage = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PINGRESP,
                false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
        log.debug("PINGREQ - clientId: {}", ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get());
        ctx.channel().writeAndFlush(pingRespMessage);
    }
}
