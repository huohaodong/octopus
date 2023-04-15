package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.service.subscription.impl.SubscriptionService;
import com.huohaodong.octopus.common.persistence.entity.RetainMessage;
import com.huohaodong.octopus.common.persistence.entity.Subscription;
import com.huohaodong.octopus.common.persistence.service.message.MessageService;
import com.huohaodong.octopus.common.protocol.mqtt.MqttPacketHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "MQTT_SUBSCRIBE")
@RequiredArgsConstructor
@Component
public class SubscribeHandler implements MqttPacketHandler<MqttSubscribeMessage> {

    private final BrokerProperties brokerProperties;

    private final MessageService messageService;

    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
        List<MqttTopicSubscription> subscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(subscriptions)) {
            List<Integer> reasonCodes = new ArrayList<>();
            subscriptions.forEach(subscription -> {
                MqttQoS subQoS = subscription.qualityOfService();
                subscriptionService.subscribe(Subscription.builder()
                        .brokerId(brokerProperties.getId())
                        .clientId(clientId)
                        .topic(subscription.topicName())
                        .qos(subQoS)
                        .build());
                reasonCodes.add(subQoS.value());
            });

            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(reasonCodes));
            ctx.channel().writeAndFlush(subAckMessage);

            List<RetainMessage> retainMessages = messageService.getAllRetainMessage(brokerProperties.getId());
            subscriptions.forEach(subscription -> getAllMatchedRetainMessage(retainMessages, subscription.topicName()).forEach(retainMessage -> {
                MqttQoS minQoS = MqttQoS.valueOf(Math.min(subscription.qualityOfService().value(), retainMessage.getQos().value()));
                int messageId = (minQoS.value() >= MqttQoS.AT_LEAST_ONCE.value()) ? messageService.acquireNextMessageId(ctx.channel()) : 0;
                MqttPublishMessage publishMessage = new MqttPublishMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, minQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessage.getTopic(), messageId),
                        Unpooled.buffer().writeBytes(retainMessage.getPayload()));
                ctx.channel().writeAndFlush(publishMessage);
            }));
        } else {
            log.error("Invalid subscription topic filter from client {} at broker {}, close connection", clientId, brokerProperties.getId());
            ctx.channel().close();
        }
    }

    private List<RetainMessage> getAllMatchedRetainMessage(List<RetainMessage> retainMessageList, String topicFilter) {
        List<RetainMessage> retainMessages = new ArrayList<>();
        retainMessageList.forEach(retainMessage -> {
            if (!topicFilter.contains("#") && !topicFilter.contains("+")) {
                if (retainMessage.getTopic().equals(topicFilter)) {
                    retainMessages.add(retainMessage);
                }
            } else {
                String topic = retainMessage.getTopic();
                String[] splitTopics = topic.split("/");
                String[] splitTopicFilters = topicFilter.split("/");
                if (splitTopics.length >= splitTopicFilters.length) {
                    StringBuilder newTopicFilter = new StringBuilder();
                    for (int i = 0; i < splitTopicFilters.length; i++) {
                        String value = splitTopicFilters[i];
                        if (value.equals("+")) {
                            newTopicFilter.append("+/");
                        } else if (value.equals("#")) {
                            newTopicFilter.append("#/");
                            break;
                        } else {
                            newTopicFilter.append(splitTopics[i]).append("/");
                        }
                    }
                    newTopicFilter = new StringBuilder(newTopicFilter.substring(0, newTopicFilter.lastIndexOf("/")));
                    if (topicFilter.contentEquals(newTopicFilter)) {
                        retainMessages.add(retainMessage);
                    }
                }
            }
        });
        return retainMessages;
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
}
