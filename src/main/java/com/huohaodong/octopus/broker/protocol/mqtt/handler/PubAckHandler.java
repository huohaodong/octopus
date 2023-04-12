package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.service.message.MessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "PUBACK")
@RequiredArgsConstructor
@Component
public class PubAckHandler implements MqttPacketHandler<MqttPubAckMessage> {

    private final BrokerProperties brokerProperties;

    private final MessageService messageService;

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPubAckMessage msg) {
        int messageId = msg.variableHeader().messageId();
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
        log.debug("Release qos 1 publish message of client {}, message id {}, at broker {}", clientId, messageId, brokerProperties.getId());
        messageService.removePublishMessage(brokerProperties.getId(), clientId, messageId);
    }
}
