package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.session.ChannelManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySessionManager implements SessionManager, ChannelManager {

    private final ConcurrentHashMap<String, Session> map = new ConcurrentHashMap<>();

    private final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void put(String clientId, Session session) {
        map.put(clientId, session);
    }

    @Override
    public Session get(String clientId) {
        return map.get(clientId);
    }

    @Override
    public Session remove(String clientId) {
        return map.remove(clientId);
    }

    @Override
    public boolean contains(String clientId) {
        return map.containsKey(clientId);
    }

    @Override
    public void addChannel(Channel channel) {
        CHANNELS.add(channel);
    }

    @Override
    public Channel getChannel(String clientId) {
        Channel channel = null;
        Session session = get(clientId);
        if (session != null) {
            channel = CHANNELS.find(session.getChannelId());
        }
        return channel;
    }
}
