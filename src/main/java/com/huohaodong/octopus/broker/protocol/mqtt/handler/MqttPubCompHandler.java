package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPubCompHandler implements MqttPacketHandler<MqttMessage> {

    private final PublishMessageManager publishMessageManager;

    public MqttPubCompHandler(PublishMessageManager publishMessageManager) {
        this.publishMessageManager = publishMessageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        log.debug("PUBCOMP - clientId: {}, messageId: {}", clientId, messageId);
        publishMessageManager.remove(clientId, messageId);
    }
}
