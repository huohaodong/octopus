package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPubRelHandler implements MqttPacketHandler<MqttMessage> {

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();

        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBCOMP,
                false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        log.debug("PUBREL - clientId: {}, messageId: {}", clientId, messageId);

        ctx.channel().writeAndFlush(pubCompMessage);
    }

}
