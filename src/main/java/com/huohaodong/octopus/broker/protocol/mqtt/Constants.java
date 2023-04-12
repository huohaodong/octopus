package com.huohaodong.octopus.broker.protocol.mqtt;

import io.netty.util.AttributeKey;

public class Constants {
    public static final String HANDLER_HEARTBEAT = "HEARTBEAT";
    public static final AttributeKey<String> CHANNEL_ATTRIBUTE_CLIENT_ID = AttributeKey.newInstance("CLIENT_ID");
}