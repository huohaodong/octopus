package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.*;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

/*
    S ---M(PUBLISH)---> R
    S <---M(PUBREC)--- R
    S ---M(PUBREL)---> R
    S <---M(PUBCOMP)--- R
    QoS2 需要保存 PUBLISH 和 PUBREL 用来保证 PUBREC 和 PUBCOMP 丢失的时候重传
    实现参考 https://juejin.cn/post/7081070560507068452
    1. 一个存储 QoS = 2 的 PUBLISH 消息的 Repo
    2. 收到 PUBLISH 消息后判断 Repo 中是否存在该消息，如果不存在则存储该消息；
    3. 返回 PUBREC
    4. 收到 PUBREL 消息后判断 Repo 中是否含有该消息，如果有则消费该消息；
    5. 返回 PUBCOMP
 */
@Slf4j
@Component
public class MqttPublishHandler implements MqttPacketHandler<MqttPublishMessage> {

    private final SessionManager sessionManager;

    private final SubscriptionManager subscriptionManager;

    private final PublishMessageManager publishMessageManager;

    private final RetainMessageManager retainMessageManager;

    private final MessageIdGenerator idGenerator;

    public MqttPublishHandler(SessionManager sessionManager, SubscriptionManager subscriptionManager, PublishMessageManager publishMessageManager, RetainMessageManager retainMessageManager, MessageIdGenerator idGenerator) {
        this.sessionManager = sessionManager;
        this.subscriptionManager = subscriptionManager;
        this.publishMessageManager = publishMessageManager;
        this.retainMessageManager = retainMessageManager;
        this.idGenerator = idGenerator;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        // TODO 重构实现，目前实现存在问题
        MqttQoS QoS = msg.fixedHeader().qosLevel();
        byte[] payload = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), payload);
        if (QoS.equals(MqttQoS.AT_MOST_ONCE)) {
            sendPublishMessage(msg.variableHeader().topicName(),
                    msg.fixedHeader().qosLevel(),
                    payload,
                    false,
                    false);
        } else if (QoS.equals(MqttQoS.AT_LEAST_ONCE)) {
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), payload, false, false);
            this.sendPubAckMessage(ctx, msg.variableHeader().packetId());
        } else if (QoS.equals(MqttQoS.EXACTLY_ONCE)) {
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), payload, false, false);
            this.sendPubRecMessage(ctx, msg.variableHeader().packetId());
        }

        if (msg.fixedHeader().isRetain()) {
            if (payload.length == 0) {
                retainMessageManager.remove(msg.variableHeader().topicName());
            } else {
                RetainMessage retainMessage = new RetainMessage(msg.variableHeader().topicName(), QoS, payload);
                retainMessageManager.put(msg.variableHeader().topicName(), retainMessage);
            }
        }
    }

    private void sendPubAckMessage(ChannelHandlerContext ctx, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(ChannelHandlerContext ctx, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        ctx.channel().writeAndFlush(pubRecMessage);
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        // TODO 目前实现并不能保证中途掉线的节点重连后能收到 QoS >= 1 的消息
        Collection<Subscription> subscriptions = subscriptionManager.getAllMatched(topic);
        if (subscriptions == null) {
            return;
        }
        subscriptions.forEach(subscription -> {
            if (sessionManager.contains(subscription.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = MqttQoS.valueOf(Math.min(mqttQoS.value(), subscription.getQoS().value()));
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscription.getClientId(), topic, respQoS.value());
                    sessionManager.get(subscription.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = idGenerator.acquireId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscription.getClientId(), topic, respQoS.value(), messageId);
                    publishMessageManager.put(new PublishMessage(subscription.getClientId(), messageId, topic, respQoS, messageBytes, MqttMessageType.PUBLISH));
                    sessionManager.get(subscription.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = idGenerator.acquireId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscription.getClientId(), topic, respQoS.value(), messageId);
                    publishMessageManager.put(new PublishMessage(subscription.getClientId(), messageId, topic, respQoS, messageBytes, MqttMessageType.PUBREL));
                    sessionManager.get(subscription.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }
}