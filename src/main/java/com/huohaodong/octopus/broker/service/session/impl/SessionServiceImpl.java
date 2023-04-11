package com.huohaodong.octopus.broker.service.session.impl;

import com.huohaodong.octopus.broker.persistence.entity.Session;
import com.huohaodong.octopus.broker.service.session.SessionService;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionServiceImpl implements SessionService {

    @Override
    public Channel getChannel(String clientId) {
        return null;
    }

    @Override
    public void putChannel(String clientId, Channel channel) {

    }

    @Override
    public void removeChannel(Channel channel) {

    }

    @Override
    public boolean containsChannel(Channel channel) {
        return false;
    }

    @Override
    public void putSession(Session session) {

    }

    @Override
    public Optional<Session> getSession(String brokerId, String clientId) {
        return Optional.empty();
    }

    @Override
    public void removeSession(String brokerId, String clientId) {

    }

    @Override
    public boolean containsSession(String brokerId, String clientId) {
        return false;
    }
}
