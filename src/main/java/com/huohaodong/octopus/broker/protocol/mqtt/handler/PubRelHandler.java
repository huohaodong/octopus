package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "PUBREL")
@Component
public class PubRelHandler implements MqttPacketHandler<MqttMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();

        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBCOMP,
                        false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubCompMessage);
    }
}
