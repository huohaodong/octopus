package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.service.message.MessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "PUBCOMP")
@RequiredArgsConstructor
@Component
public class PubCompHandler implements MqttPacketHandler<MqttMessage> {

    private final BrokerProperties brokerProperties;

    private final MessageService messageService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
        log.debug("Release qos 2 publish release message of client {}, message id {}, at broker {}", clientId, messageId, brokerProperties.getId());
        messageService.removePublishReleaseMessage(brokerProperties.getId(), clientId, messageId);
        messageService.releaseMessageId(ctx.channel(), messageId);
    }
}
