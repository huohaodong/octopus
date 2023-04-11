package com.huohaodong.octopus.broker.service.session;

import io.netty.channel.Channel;

public interface ChannelManager {
    Channel getChannelByClientId(String clientId);

    void addChannel(String clientId, Channel channel);

    void closeChannel(Channel channel);
}
