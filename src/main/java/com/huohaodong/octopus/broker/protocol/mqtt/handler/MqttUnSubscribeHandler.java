package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.server.metric.annotation.ReceivedMetric;
import com.huohaodong.octopus.broker.server.metric.aspect.StatsCollector;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class MqttUnSubscribeHandler implements MqttPacketHandler<MqttUnsubscribeMessage> {

    private final StatsCollector statsCollector;

    private final SubscriptionManager subscriptionManager;

    @Override
    @ReceivedMetric
    public void doProcess(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        List<String> topicFilters = msg.payload().topics();
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        topicFilters.forEach(topicFilter -> {
            subscriptionManager.unSubscribe(clientId, topicFilter);
        });
        MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        statsCollector.getDeltaTotalSent().incrementAndGet();
        ctx.channel().writeAndFlush(unsubAckMessage);
    }
}
