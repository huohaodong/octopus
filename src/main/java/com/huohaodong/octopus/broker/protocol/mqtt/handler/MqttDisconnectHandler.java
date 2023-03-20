package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttDisconnectHandler implements MqttPacketHandler<MqttMessage> {

    private SessionManager sessionManager;

    private SubscriptionManager subscriptionManager;

    private PublishMessageManager publishMessageManager;

    public MqttDisconnectHandler(SessionManager sessionManager, SubscriptionManager subscriptionManager, PublishMessageManager publishMessageManager) {
        this.sessionManager = sessionManager;
        this.subscriptionManager = subscriptionManager;
        this.publishMessageManager = publishMessageManager;
    }

    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttMessage msg) {
        String clientId = (String) ctx.channel().attr(AttributeKey.valueOf("CLIENT_ID")).get();
        Session session = sessionManager.get(clientId);
        if (session.isCleanSession()) {
            subscriptionManager.unSubscribeAll(clientId);
            publishMessageManager.removeAllByClientId(clientId);
        }
        sessionManager.remove(clientId);
        ctx.channel().close();
    }
}
