package com.huohaodong.octopus.broker.store.session;

import io.netty.channel.Channel;
import org.springframework.lang.Nullable;

public interface ChannelManager {

    void addChannel(Channel channel);

    @Nullable
    Channel getChannel(String clientId);

}
