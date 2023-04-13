package com.huohaodong.octopus.broker.protocol.mqtt;

import com.huohaodong.octopus.broker.protocol.mqtt.handler.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}
