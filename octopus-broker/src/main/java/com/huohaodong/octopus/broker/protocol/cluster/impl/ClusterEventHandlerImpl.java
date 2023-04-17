package com.huohaodong.octopus.broker.protocol.cluster.impl;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.protocol.mqtt.handler.PublishHandler;
import com.huohaodong.octopus.common.persistence.entity.PublishMessage;
import com.huohaodong.octopus.common.persistence.service.message.MessageService;
import com.huohaodong.octopus.common.persistence.service.session.SessionService;
import com.huohaodong.octopus.common.persistence.service.subscription.SubscriptionService;
import com.huohaodong.octopus.common.protocol.cluster.handler.ClusterEventHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j(topic = "CLUSTER_EVENT_HANDLER")
@RequiredArgsConstructor
@Service
public class ClusterEventHandlerImpl implements ClusterEventHandler {

    private final BrokerProperties brokerProperties;

    private final PublishHandler publishHandler;

    private final SessionService sessionService;

    private final MessageService messageService;

    private final SubscriptionService subscriptionService;

    @Override
    public void doPublish(PublishMessage publishMessage) {
        publishHandler.sendPublishMessage(publishMessage.getTopic(), publishMessage.getQos(), publishMessage.getPayload());
    }

    @Override
    public void doCloseChannel(String clientId) {
        sessionService.getChannelByClientId(clientId).ifPresent(channel -> {
            try {
                channel.writeAndFlush(MqttMessage.DISCONNECT).addListener((ChannelFutureListener) future -> channel.close()).sync();
                sessionService.getSession(brokerProperties.getId(), clientId).ifPresent(session -> {
                    if (session.isCleanSession()) {
                        messageService.removeAllPublishMessage(session.getBrokerId(), session.getClientId());
                        messageService.removeAllPublishReleaseMessage(session.getBrokerId(), session.getClientId());
                        subscriptionService.unSubscribeAll(brokerProperties.getId(), clientId);
                    }
                    sessionService.removeSession(brokerProperties.getId(), clientId);
                });
            } catch (InterruptedException e) {
                log.error("Error occurs when close duplicate login client {}", clientId);
                throw new RuntimeException(e);
            }
        });
    }
}
