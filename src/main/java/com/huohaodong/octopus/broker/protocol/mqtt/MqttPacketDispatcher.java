package com.huohaodong.octopus.broker.protocol.mqtt;

import com.huohaodong.octopus.broker.protocol.mqtt.handler.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
@ChannelHandler.Sharable
public class MqttPacketDispatcher extends SimpleChannelInboundHandler<MqttMessage> {
    private final MqttConnectHandler mqttConnectHandler;

    private final MqttPublishHandler mqttPublishHandler;

    private final MqttPubAckHandler mqttPubAckHandler;

    private final MqttPubRecHandler mqttPubRecHandler;

    private final MqttPubRelHandler mqttPubRelHandler;

    private final MqttPubCompHandler mqttPubCompHandler;

    private final MqttSubscribeHandler mqttSubscribeHandler;

    private final MqttUnSubscribeHandler mqttUnSubscribeHandler;

    private final MqttPingHandler mqttPingHandler;

    private final MqttDisconnectHandler mqttDisconnectHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        MqttMessageType msgType = msg.fixedHeader().messageType();
        log.info("received {} message", msgType);
        switch (msgType) {
            case CONNECT:
                mqttConnectHandler.doProcess(ctx, (MqttConnectMessage) msg);
                break;
            case PUBLISH:
                mqttPublishHandler.doProcess(ctx, (MqttPublishMessage) msg);
                break;
            case PUBACK:
                mqttPubAckHandler.doProcess(ctx, (MqttPubAckMessage) msg);
                break;
            case PUBREC:
                mqttPubRecHandler.doProcess(ctx, msg);
                break;
            case PUBREL:
                mqttPubRelHandler.doProcess(ctx, msg);
                break;
            case PUBCOMP:
                mqttPubCompHandler.doProcess(ctx, msg);
                break;
            case SUBSCRIBE:
                mqttSubscribeHandler.doProcess(ctx, (MqttSubscribeMessage) msg);
                break;
            case UNSUBSCRIBE:
                mqttUnSubscribeHandler.doProcess(ctx, (MqttUnsubscribeMessage) msg);
                break;
            case PINGREQ:
                mqttPingHandler.doProcess(ctx, msg);
                break;
            case DISCONNECT:
                mqttDisconnectHandler.doProcess(ctx, msg);
                break;
            case AUTH:
                break;
            default:
                ctx.channel().close();
                throw new UnsupportedMessageTypeException(msg.decoderResult().cause(), MqttMessageType.class);
        }
    }

    /* 心跳包 */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                log.info("heartbeat timeout, close channel");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
