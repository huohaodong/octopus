package com.huohaodong.octopus.broker.service.session;

import io.netty.channel.Channel;

import java.util.Optional;

public interface ChannelManager {
    Optional<Channel> getChannelByClientId(String clientId);

    void addChannel(String clientId, Channel channel);

    void closeChannel(Channel channel);
}
