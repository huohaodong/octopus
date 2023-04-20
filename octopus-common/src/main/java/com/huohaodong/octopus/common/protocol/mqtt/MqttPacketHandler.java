package com.huohaodong.octopus.common.protocol.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface MqttPacketHandler<T extends MqttMessage> {

    void doProcess(ChannelHandlerContext ctx, T msg);

}
