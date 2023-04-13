package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.persistence.entity.PublishMessage;
import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;
import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;
import com.huohaodong.octopus.broker.persistence.entity.Subscription;
import com.huohaodong.octopus.broker.service.message.MessageService;
import com.huohaodong.octopus.broker.service.session.SessionService;
import com.huohaodong.octopus.broker.service.subscription.impl.SubscriptionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "PUBLISH")
@RequiredArgsConstructor
@Component
public class PublishHandler implements MqttPacketHandler<MqttPublishMessage> {

    private final BrokerProperties brokerProperties;

    private final MessageService messageService;

    private final SessionService sessionService;

    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        MqttQoS reqQoS = msg.fixedHeader().qosLevel();
        byte[] payload = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), payload);
        switch (reqQoS) {
            case AT_MOST_ONCE ->
                    sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), payload);
            case AT_LEAST_ONCE -> {
                sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), payload);
                sendPubAckMessage(ctx, msg.variableHeader().packetId());
            }
            case EXACTLY_ONCE -> {
                sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), payload);
                sendPubRecMessage(ctx, msg.variableHeader().packetId());
            }
        }
        if (msg.fixedHeader().isRetain()) {
            if (payload.length == 0) {
                messageService.removeRetainMessage(brokerProperties.getId(), msg.variableHeader().topicName());
            } else {
                messageService.putRetainMessage(RetainMessage.builder()
                        .brokerId(brokerProperties.getId())
                        .clientId(ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get())
                        .topic(msg.variableHeader().topicName())
                        .qos(reqQoS)
                        .payload(payload)
                        .build());
            }
        }
    }

    private void sendPubAckMessage(ChannelHandlerContext ctx, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(ChannelHandlerContext ctx, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubRecMessage);
    }

    private void sendPublishMessage(String topic, MqttQoS QoS, byte[] payload) {
        Collection<Subscription> subscriptions = subscriptionService.getAllMatched(brokerProperties.getId(), topic);
        subscriptions.forEach(subscription -> {
            String clientId = subscription.getClientId();
            sessionService.getChannelByClientId(clientId).ifPresent(channel -> {
                MqttQoS respQoS = MqttQoS.valueOf(Math.min(QoS.value(), subscription.getQos().value()));
                int respMessageId = (respQoS.value() >= MqttQoS.AT_LEAST_ONCE.value()) ? messageService.acquireNextMessageId(channel) : 0;
                ByteBuf respMessagePayload = Unpooled.buffer().writeBytes(payload);
                MqttPublishMessage respPublishMessage = new MqttPublishMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(topic, respMessageId),
                        respMessagePayload
                );
                switch (respQoS) {
                    case AT_LEAST_ONCE -> messageService.putPublishMessage(PublishMessage.builder()
                            .brokerId(brokerProperties.getId())
                            .clientId(clientId)
                            .messageId(respMessageId)
                            .topic(topic)
                            .payload(payload)
                            .qos(respQoS)
                            .build());
                    case EXACTLY_ONCE -> messageService.putPublishReleaseMessage(PublishReleaseMessage.builder()
                            .brokerId(brokerProperties.getId())
                            .clientId(clientId)
                            .messageId(respMessageId)
                            .build());
                }
                channel.writeAndFlush(respPublishMessage);
            });
        });
    }
}
