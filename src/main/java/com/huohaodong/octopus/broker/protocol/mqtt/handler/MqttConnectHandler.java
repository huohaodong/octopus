package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class MqttConnectHandler implements MqttPacketHandler<MqttConnectMessage> {

    private SessionManager sessionManager;

    private SubscriptionManager subscriptionManager;

    private PublishMessageManager publishMessageManager;

    public MqttConnectHandler(SessionManager sessionManager, SubscriptionManager subscriptionManager, PublishMessageManager publishMessageManager) {
        this.sessionManager = sessionManager;
        this.subscriptionManager = subscriptionManager;
        this.publishMessageManager = publishMessageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        Channel channel = ctx.channel();

        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            MqttConnectReturnCode returnCode = null;
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                log.info("Unsupported protocol version");
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                log.info("Invalid clientId");
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            }
            if (returnCode != null) {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(returnCode, false), null);
                channel.writeAndFlush(connAckMessage);
            }
            channel.close();
            return;
        }

        String clientId = msg.payload().clientIdentifier();
        if (clientId.isEmpty()) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        }
        // TODO Auth

        if (sessionManager.contains(clientId)) {
            // TODO 查询持久化的连接，发现重复则踢人，踢人也需要注意在集群间广播消息
            log.info("Duplicated connection of client {}, close connection", clientId);
            Session session = sessionManager.get(clientId);
            Channel previous = session.getChannel();
            if (session.isCleanSession()) {
                sessionManager.remove(clientId);
                subscriptionManager.unSubscribeAll(clientId);
                publishMessageManager.removeAllByClientId(clientId);
            }
            previous.close();
        }

        Session session = new Session(msg.payload().clientIdentifier(), channel, msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()) {
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
            session.setWillMessage(willMessage);
        }
        sessionManager.put(clientId, session);

        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("heartbeat")) {
                channel.pipeline().remove("heartbeat");
            }
            channel.pipeline().addLast("heartbeat", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }

        channel.attr(AttributeKey.valueOf("CLIENT_ID")).set(clientId);

        boolean sessionPresent = sessionManager.contains(clientId) && !session.isCleanSession();
        MqttConnAckMessage connAck = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        channel.writeAndFlush(connAck);

        if (!session.isCleanSession()) {
            Collection<PublishMessage> messages = publishMessageManager.getAllByClientId(clientId);
            // TODO 改为 Optional 接口
            if (messages != null) {
                messages.forEach(message -> {
                    if (message.getType().equals(MqttMessageType.PUBLISH)) {
                        MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                                new MqttFixedHeader(MqttMessageType.PUBLISH, true, message.getQoS(), false, 0),
                                new MqttPublishVariableHeader(message.getTopic(), message.getMessageId()), Unpooled.buffer().writeBytes(message.getPayload()));
                        channel.writeAndFlush(publishMessage);
                    } else if (message.getType().equals(MqttMessageType.PUBREL)) {
                        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                                new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                                MqttMessageIdVariableHeader.from(message.getMessageId()), null);
                        channel.writeAndFlush(pubRelMessage);
                    } else {
                        log.warn("Unknown Message Type: {}", message.getType());
                    }
                });
            }
        }
    }
}
