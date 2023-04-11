package com.huohaodong.octopus.broker.service.session;

import io.netty.channel.Channel;

public interface ChannelManager {
    Channel getChannel(String clientId);

    void putChannel(String clientId, Channel channel);

    void removeChannel(Channel channel);

    boolean containsChannel(Channel channel);
}
