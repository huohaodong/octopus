package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttPubAckHandler implements MqttPacketHandler<MqttPubAckMessage> {

    private final PublishMessageManager publishMessageManager;

    public MqttPubAckHandler(PublishMessageManager publishMessageManager) {
        this.publishMessageManager = publishMessageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPubAckMessage msg) {
        int messageId = msg.variableHeader().messageId();
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        publishMessageManager.remove(clientId, messageId);
    }
}
