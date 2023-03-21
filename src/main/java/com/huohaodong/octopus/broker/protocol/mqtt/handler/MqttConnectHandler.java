package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerConfig;
import com.huohaodong.octopus.broker.server.cluster.ClusterEventManager;
import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.session.ChannelManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.session.WillMessage;
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

    private final BrokerConfig brokerConfig;

    private final SessionManager sessionManager;

    private final SubscriptionManager subscriptionManager;

    private final PublishMessageManager publishMessageManager;

    private final ClusterEventManager clusterEventManager;

    private final ChannelManager channelManager;

    public MqttConnectHandler(BrokerConfig brokerConfig, SessionManager sessionManager, SubscriptionManager subscriptionManager, PublishMessageManager publishMessageManager, ClusterEventManager clusterEventManager, ChannelManager channelManager) {
        this.brokerConfig = brokerConfig;
        this.sessionManager = sessionManager;
        this.subscriptionManager = subscriptionManager;
        this.publishMessageManager = publishMessageManager;
        this.clusterEventManager = clusterEventManager;
        this.channelManager = channelManager;
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

        // 关闭本 Broker 内部重复的连接
        if (sessionManager.contains(clientId)) {
            log.info("Duplicated connection of client {}, close connection", clientId);
            Session session = sessionManager.get(clientId);
            Channel previous = channelManager.getChannel(clientId);
            if (session.isCleanSession()) {
                sessionManager.remove(clientId);
                subscriptionManager.unSubscribeAll(clientId);
                publishMessageManager.removeAllByClientId(clientId);
            }
            if (previous != null) {
                previous.close();
            }
        }
        // 关闭同一组 Broker Group 内部的其他 Broker 上的重复连接
        clusterEventManager.broadcastToClose(clientId);

        Session session = new Session(brokerConfig.getGroup(), brokerConfig.getId(), clientId, ctx.channel().id(), msg.variableHeader().isCleanSession(), null);

        if (msg.variableHeader().isWillFlag()) {
            WillMessage willMessage = new WillMessage(
                    msg.payload().willTopic(),
                    MqttQoS.valueOf(msg.variableHeader().willQos()),
                    msg.payload().willMessageInBytes().clone(),
                    msg.variableHeader().isWillRetain()
                    );
            session.setWillMessage(willMessage);
        }
        sessionManager.put(clientId, session);

        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("heartbeat")) {
                channel.pipeline().remove("heartbeat");
            }
            channel.pipeline().addLast("heartbeat", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }

        // TODO 检查是否还需要这样实现
        channel.attr(AttributeKey.valueOf("CLIENT_ID")).set(clientId);

        boolean sessionPresent = sessionManager.contains(clientId) && !session.isCleanSession();
        MqttConnAckMessage connAck = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        channel.writeAndFlush(connAck);

        if (!session.isCleanSession()) {
            Collection<PublishMessage> messages = publishMessageManager.getAllByClientId(clientId);
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
