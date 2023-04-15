package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.common.protocol.mqtt.MqttPacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "MQTT_PING")
@Component
public class PingHandler implements MqttPacketHandler<MqttMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        ctx.channel().writeAndFlush(MqttMessage.PINGRESP);
    }
}
