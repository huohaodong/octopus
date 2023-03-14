package com.huohaodong.octopus.broker.protocol.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.springframework.stereotype.Component;

@Component
public class MqttPublishHandler implements MqttPacketHandler<MqttPublishMessage> {
    @Override
    public void doProcess(ChannelHandlerContext ctx, MqttPublishMessage msg) {

    }
}
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
