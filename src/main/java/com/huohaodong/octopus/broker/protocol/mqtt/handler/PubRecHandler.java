package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;
import com.huohaodong.octopus.broker.service.message.MessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "MQTT_PUBREC")
@RequiredArgsConstructor
@Component
public class PubRecHandler implements MqttPacketHandler<MqttMessage> {

    private final BrokerProperties brokerProperties;

    private final MessageService messageService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();

        PublishReleaseMessage publishReleaseMessage = PublishReleaseMessage.builder()
                .brokerId(brokerProperties.getId())
                .clientId(clientId)
                .messageId(messageId)
                .build();

        log.debug("Release qos 2 publish message of client {}, message id {}, at broker {}", clientId, messageId, brokerProperties.getId());
        messageService.removePublishMessage(brokerProperties.getId(), clientId, messageId);
        log.debug("Persist qos 2 publish release message from client {}, message id {}, at broker {}", clientId, messageId, brokerProperties.getId());
        messageService.putPublishReleaseMessage(publishReleaseMessage);

        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubRelMessage);
    }
}
