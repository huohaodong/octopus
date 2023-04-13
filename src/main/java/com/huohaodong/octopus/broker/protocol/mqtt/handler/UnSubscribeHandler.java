package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.service.subscription.impl.SubscriptionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "UNSUBSCRIBE")
@RequiredArgsConstructor
@Component
public class UnSubscribeHandler implements MqttPacketHandler<MqttUnsubscribeMessage> {

    private final BrokerProperties brokerProperties;

    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
        List<String> topics = msg.payload().topics();
        topics.forEach(topic -> subscriptionService.unSubscribe(brokerProperties.getId(), clientId, topic));
        MqttUnsubAckMessage unsubAckMessage = new MqttUnsubAckMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        ctx.channel().writeAndFlush(unsubAckMessage);
    }
}
