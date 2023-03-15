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
            List<Integer> mqttQoSList = new ArrayList<Integer>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                Subscription subscription = new Subscription(clientId, topicFilter, mqttQoS);
                subscriptionManager.subscribe(subscription);
                mqttQoSList.add(mqttQoS.value());
                log.debug("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            });
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(mqttQoSList));
            ctx.channel().writeAndFlush(subAckMessage);
            // 发布保留消息
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                this.sendRetainMessage(ctx.channel(), topicFilter, mqttQoS);
            });
        } else {
            ctx.channel().close();
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // 以#或+符号开头的、以/符号结尾的及不存在/符号的订阅按非法订阅处理, 这里没有参考标准协议
            if (topicFilter.startsWith("#") || topicFilter.startsWith("+") || topicFilter.endsWith("/") || !topicFilter.contains("/")) {
                return false;
            }
            if (topicFilter.contains("#")) {
                // 不是以/#字符串结尾的订阅按非法订阅处理
                if (!topicFilter.endsWith("/#")) {
                    return false;
                }
                // 如果出现多个#符号的订阅按非法订阅处理
                if (topicFilter.indexOf("#") != topicFilter.lastIndexOf("#")) {
                    return false;
                }
            }
            if (topicFilter.contains("+")) {
                //如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
                if (StringUtils.countOccurrencesOf(topicFilter, "+") != StringUtils.countOccurrencesOf(topicFilter, "/+")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQoS) {
        Collection<RetainMessage> retainMessages = retainMessageManager.getAllMatched(topicFilter);
        if (retainMessages == null) {
            return;
        }
        retainMessages.forEach(retainMessage -> {
            MqttQoS respQoS = MqttQoS.valueOf(Math.min(mqttQoS.value(), retainMessage.getQoS().value()));
            if (respQoS == MqttQoS.AT_MOST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessage.getTopic(), 0), Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessage.getTopic(), respQoS.value());
                channel.writeAndFlush(publishMessage);
            }
            int messageId = idGenerator.acquireId();
            if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessage.getTopic(), messageId), Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessage.getTopic(), respQoS.value(), messageId);
                channel.writeAndFlush(publishMessage);
            }
            if (respQoS == MqttQoS.EXACTLY_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessage.getTopic(), messageId), Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessage.getTopic(), respQoS.value(), messageId);
                channel.writeAndFlush(publishMessage);
            }
        });
    }
}
