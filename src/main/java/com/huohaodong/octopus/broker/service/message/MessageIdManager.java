package com.huohaodong.octopus.broker.service.message;

import io.netty.channel.Channel;

public interface MessageIdManager {
    int acquireNextMessageId(Channel channel);

    void releaseMessageId(Channel channel, int messageId);
}
