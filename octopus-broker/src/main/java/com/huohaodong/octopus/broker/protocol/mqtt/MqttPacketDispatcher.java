package com.huohaodong.octopus.broker.protocol.mqtt;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.protocol.mqtt.handler.*;
import com.huohaodong.octopus.common.persistence.service.message.MessageService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "MQTT_DISPATCHER")
@RequiredArgsConstructor
@Component
@ChannelHandler.Sharable
public class MqttPacketDispatcher extends SimpleChannelInboundHandler<MqttMessage> {
    private final ConnectHandler connectHandler;

    private final PublishHandler publishHandler;

    private final PubAckHandler pubAckHandler;

    private final PubRecHandler pubRecHandler;

    private final PubRelHandler pubRelHandler;

    private final PubCompHandler pubCompHandler;

    private final SubscribeHandler subscribeHandler;

    private final UnSubscribeHandler unSubscribeHandler;

    private final PingHandler pingHandler;

    private final DisconnectHandler disconnectHandler;

    private final MessageService messageService;

    private final BrokerProperties brokerProperties;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        MqttMessageType msgType = msg.fixedHeader().messageType();
        log.info("Received {} message", msgType);
        switch (msgType) {
            case CONNECT:
                connectHandler.doProcess(ctx, (MqttConnectMessage) msg);
                break;
            case PUBLISH:
                publishHandler.doProcess(ctx, (MqttPublishMessage) msg);
                break;
            case PUBACK:
                pubAckHandler.doProcess(ctx, (MqttPubAckMessage) msg);
                break;
            case PUBREC:
                pubRecHandler.doProcess(ctx, msg);
                break;
            case PUBREL:
                pubRelHandler.doProcess(ctx, msg);
                break;
            case PUBCOMP:
                pubCompHandler.doProcess(ctx, msg);
                break;
            case SUBSCRIBE:
                subscribeHandler.doProcess(ctx, (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                unSubscribeHandler.doProcess(ctx, (MqttUnsubscribeMessage) msg);
                break;
            case PINGREQ:
                pingHandler.doProcess(ctx, msg);
                break;
            case DISCONNECT:
                disconnectHandler.doProcess(ctx, msg);
                break;
            case AUTH:
                break;
            default:
                ctx.channel().close();
                throw new UnsupportedMessageTypeException(msg.decoderResult().cause(), MqttMessageType.class);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleStateEvent) {
            String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                log.info("heartbeat timeout, close channel");
                messageService.getWillMessage(brokerProperties.getId(), clientId).ifPresent(willMessage -> publishHandler.sendPublishMessage(willMessage.getTopic(), willMessage.getQos(), willMessage.getPayload()));
                disconnectHandler.doProcess(ctx, null);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
