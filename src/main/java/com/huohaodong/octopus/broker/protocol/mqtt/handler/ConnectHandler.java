package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.persistence.entity.*;
import com.huohaodong.octopus.broker.service.message.MessageService;
import com.huohaodong.octopus.broker.service.session.SessionService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.HANDLER_HEARTBEAT;

@Slf4j(topic = "CONNECT")
@RequiredArgsConstructor
@Component
public class ConnectHandler implements MqttPacketHandler<MqttConnectMessage> {

    private final BrokerProperties brokerProperties;

    private final SessionService sessionService;

    private final MessageService messageService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        Channel channel = ctx.channel();

        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            MqttConnectReturnCode returnCode = null;
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                log.debug("Unsupported protocol version");
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                log.debug("Invalid Client Id");
                returnCode = MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
            } else {
                log.debug(cause.toString());
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

        if (brokerProperties.getAuth().isEnable()) {
            if (msg.variableHeader().hasUserName() && msg.variableHeader().hasPassword()) {
                // TODO 验证用户名密码是否合理
                // TODO 验证是否有权限访问本 broker，如果没有权限则返回 CONNECTION_REFUSED_NOT_AUTHORIZED
            } else {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
                channel.writeAndFlush(connAckMessage);
                channel.close();
                return;
            }
        }

        Optional<Session> oldSessionLocal = sessionService.getSession(brokerProperties.getId(), clientId);
        oldSessionLocal.ifPresent(oldSession -> {
            if (oldSession.getStatus().equals(SessionStatus.ONLINE)) {
                log.info("Duplicated connection of client {} at broker {}, close connection", clientId, brokerProperties.getId());
                sessionService.getChannelByClientId(oldSession.getClientId()).ifPresent(ChannelOutboundInvoker::close);
            }
            if (oldSession.isCleanSession()) {
                sessionService.removeSession(oldSession.getBrokerId(), oldSession.getClientId());
                messageService.removeAllPublishMessage(brokerProperties.getId(), oldSession.getClientId());
                messageService.removeAllPublishReleaseMessage(brokerProperties.getId(), oldSession.getClientId());
            }
        });

        Session curSession = Session.builder()
                .brokerIp(brokerProperties.getHost())
                .brokerId(brokerProperties.getId())
                .clientIp(channel.remoteAddress().toString())
                .clientId(clientId)
                .status(SessionStatus.ONLINE)
                .build();

        sessionService.addChannel(clientId, channel);
        sessionService.putSession(curSession);

        if (!curSession.isCleanSession()) {
            List<PublishMessage> unProcessedPublishMessages = messageService.getAllPublishMessage(brokerProperties.getId(), clientId);
            List<PublishReleaseMessage> unProcessedPublishReleaseMessages = messageService.getAllPublishReleaseMessage(brokerProperties.getId(), clientId);
            unProcessedPublishMessages.forEach(message -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, true, message.getQos(), false, 0),
                        new MqttPublishVariableHeader(message.getTopic(), message.getMessageId()), Unpooled.buffer().writeBytes(message.getPayload()));
                channel.writeAndFlush(publishMessage);
            });
            unProcessedPublishReleaseMessages.forEach(message -> {
                MqttMessage publishReleaseMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(message.getMessageId()), null);
                channel.writeAndFlush(publishReleaseMessage);
            });
        }

        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains(HANDLER_HEARTBEAT)) {
                channel.pipeline().remove(HANDLER_HEARTBEAT);
            }
            channel.pipeline().addLast(HANDLER_HEARTBEAT, new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }

        if (msg.variableHeader().isWillFlag()) {
            WillMessage willMessage = WillMessage.builder()
                    .brokerId(brokerProperties.getId())
                    .clientId(clientId)
                    .topic(msg.payload().willTopic())
                    .qos(MqttQoS.valueOf(msg.variableHeader().willQos()))
                    .payload(msg.payload().willMessageInBytes().clone())
                    .retain(msg.variableHeader().isWillRetain())
                    .build();
            messageService.putWillMessage(willMessage);
        }

        boolean sessionPresent = oldSessionLocal.isPresent() && !curSession.isCleanSession();
        MqttConnAckMessage connAck = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        channel.writeAndFlush(connAck);
    }
}
