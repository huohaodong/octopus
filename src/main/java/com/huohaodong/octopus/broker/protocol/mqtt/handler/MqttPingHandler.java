package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.server.metric.annotation.ReceivedMetric;
import com.huohaodong.octopus.broker.server.metric.aspect.StatsCollector;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class MqttPingHandler implements MqttPacketHandler<MqttMessage> {

    private final StatsCollector statsCollector;

    @Override
    @ReceivedMetric
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        statsCollector.getDeltaTotalSent().incrementAndGet();
        ctx.channel().writeAndFlush(MqttPublishMessage.PINGRESP);
    }
}
