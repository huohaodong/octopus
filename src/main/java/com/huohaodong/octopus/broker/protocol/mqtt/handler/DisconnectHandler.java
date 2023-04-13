package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.persistence.entity.Session;
import com.huohaodong.octopus.broker.service.message.MessageService;
import com.huohaodong.octopus.broker.service.session.SessionService;
import com.huohaodong.octopus.broker.service.subscription.impl.SubscriptionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.huohaodong.octopus.broker.protocol.mqtt.Constants.CHANNEL_ATTRIBUTE_CLIENT_ID;

@Slf4j(topic = "MQTT_DISCONNECT")
@RequiredArgsConstructor
@Component
public class DisconnectHandler implements MqttPacketHandler<MqttMessage> {

    private final BrokerProperties brokerProperties;

    private final SessionService sessionService;

    private final SubscriptionService subscriptionService;

    private final MessageService messageService;

    @Override
    @Transactional
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        String clientId = ctx.channel().attr(CHANNEL_ATTRIBUTE_CLIENT_ID).get();
        Optional<Session> curSession = sessionService.getSession(brokerProperties.getId(), clientId);
        curSession.ifPresent(session -> {
            if (session.isCleanSession()) {
                subscriptionService.unSubscribeAll(brokerProperties.getId(), clientId);
                messageService.removeAllPublishMessage(brokerProperties.getId(), clientId);
                messageService.removeAllPublishReleaseMessage(brokerProperties.getId(), clientId);
            }
            sessionService.removeSession(brokerProperties.getId(), clientId);
        });
        log.debug("Close connection of client {} at broker {}", clientId, brokerProperties.getId());
        ctx.channel().close();
    }
}
