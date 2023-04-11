package com.huohaodong.octopus.broker.service.session.impl;

import com.huohaodong.octopus.broker.persistence.entity.Session;
import com.huohaodong.octopus.broker.persistence.repository.SessionRepository;
import com.huohaodong.octopus.broker.service.session.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ConcurrentReferenceHashMap<String, Channel> clientIdToChannelMap = new ConcurrentReferenceHashMap<>();

    private final SessionRepository sessionRepository;

    @Override
    public Channel getChannelByClientId(String clientId) {
        return clientIdToChannelMap.get(clientId);
    }

    @Override
    public void addChannel(String clientId, Channel channel) {
        clientIdToChannelMap.put(clientId, channel);
    }

    @Override
    public void closeChannel(Channel channel) {
        channel.closeFuture().addListener((ChannelFutureListener) future -> clientIdToChannelMap.remove(channel));
    }

    @Override
    public void putSession(Session session) {
        sessionRepository.save(session);
    }

    @Override
    public Optional<Session> getSession(String brokerId, String clientId) {
        return sessionRepository.findByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public void removeSession(String brokerId, String clientId) {
        sessionRepository.deleteByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public boolean containsSession(String brokerId, String clientId) {
        return sessionRepository.existsByBrokerIdAndClientId(brokerId, clientId);
    }
}
