package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPubRecHandler implements MqttPacketHandler<MqttMessage> {

    private final PublishMessageManager publishMessageManager;

    public MqttPubRecHandler(PublishMessageManager messageManager) {
        this.publishMessageManager = messageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();

        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        log.debug("PUBREC - clientId: {}, messageId: {}", clientId, messageId);

        publishMessageManager.remove(clientId, messageId);
        PublishMessage message = new PublishMessage(clientId, messageId);
        publishMessageManager.put(message);

        ctx.channel().writeAndFlush(pubRelMessage);
    }
}
