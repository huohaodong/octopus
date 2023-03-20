package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.MessageIdGenerator;
import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessageManager;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class MqttSubscribeHandler implements MqttPacketHandler<MqttSubscribeMessage> {

    private final SubscriptionManager subscriptionManager;

    private final MessageIdGenerator idGenerator;

    private final RetainMessageManager retainMessageManager;

    public MqttSubscribeHandler(SubscriptionManager subscriptionManager, MessageIdGenerator idGenerator, RetainMessageManager retainMessageManager) {
        this.subscriptionManager = subscriptionManager;
        this.idGenerator = idGenerator;
        this.retainMessageManager = retainMessageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (topicSubscriptions == null) {
            return;
        }
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
            List<Integer> QoSList = new ArrayList<Integer>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                Subscription subscription = new Subscription(clientId, topicFilter, mqttQoS);
                subscriptionManager.subscribe(subscription);
                QoSList.add(mqttQoS.value());
            });
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(QoSList));
            ctx.channel().writeAndFlush(subAckMessage);
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS QoS = topicSubscription.qualityOfService();
                this.sendRetainMessage(ctx.channel(), topicFilter, QoS);
            });
        } else {
            ctx.channel().close();
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            if (topicFilter.startsWith("#") || topicFilter.startsWith("+") || topicFilter.endsWith("/") || !topicFilter.contains("/")) {
                return false;
            }
            if (topicFilter.contains("#")) {
                if (!topicFilter.endsWith("/#")) {
                    return false;
                }
                if (topicFilter.indexOf("#") != topicFilter.lastIndexOf("#")) {
                    return false;
                }
            }
            if (topicFilter.contains("+")) {
                if (StringUtils.countOccurrencesOf(topicFilter, "+") != StringUtils.countOccurrencesOf(topicFilter, "/+")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS QoS) {
        Collection<RetainMessage> retainMessages = retainMessageManager.getAllMatched(topicFilter);
        if (retainMessages == null) {
            return;
        }
        retainMessages.forEach(retainMessage -> {
            MqttPublishMessage publishMessage = null;
            MqttQoS minQoS = MqttQoS.valueOf(Math.min(QoS.value(), retainMessage.getQoS().value()));
            if (minQoS == MqttQoS.AT_MOST_ONCE) {
                publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, minQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessage.getTopic(), 0), Unpooled.buffer().writeBytes(retainMessage.getPayload()));

            } else {
                int messageId = idGenerator.acquireId();
                if (minQoS == MqttQoS.AT_LEAST_ONCE) {
                    publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, false, minQoS, false, 0),
                            new MqttPublishVariableHeader(retainMessage.getTopic(), messageId), Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                }
                if (minQoS == MqttQoS.EXACTLY_ONCE) {
                    publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, false, minQoS, false, 0),
                            new MqttPublishVariableHeader(retainMessage.getTopic(), messageId), Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                }
            }
            if (publishMessage != null) {
                channel.writeAndFlush(publishMessage);
            }
        });
    }
}
