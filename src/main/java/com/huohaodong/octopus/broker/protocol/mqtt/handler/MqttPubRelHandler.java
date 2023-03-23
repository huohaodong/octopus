package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.server.metric.annotation.ReceivedMetric;
import com.huohaodong.octopus.broker.server.metric.aspect.StatsCollector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class MqttPubRelHandler implements MqttPacketHandler<MqttMessage> {

    private final StatsCollector statsCollector;

    @Override
    @ReceivedMetric
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        MqttMessageIdVariableHeader header = (MqttMessageIdVariableHeader) msg.variableHeader();
        int messageId = header.messageId();

        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBCOMP,
                        false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        statsCollector.getDeltaTotalSent().incrementAndGet();
        ctx.channel().writeAndFlush(pubCompMessage);
    }

}
