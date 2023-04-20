package com.huohaodong.octopus.broker.service.session;

import com.huohaodong.octopus.common.persistence.entity.Session;
import com.huohaodong.octopus.common.persistence.repository.SessionRepository;
import com.huohaodong.octopus.common.persistence.service.session.SessionService;
import com.huohaodong.octopus.exporter.metric.annotation.GaugeDecMetric;
import com.huohaodong.octopus.exporter.metric.annotation.GaugeIncMetric;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.List;
import java.util.Optional;

import static com.huohaodong.octopus.exporter.metric.Constants.METRIC_CONNECTION_ACTIVE;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ConcurrentReferenceHashMap<String, Channel> clientIdToChannelMap = new ConcurrentReferenceHashMap<>(32);

    private final SessionRepository sessionRepository;

    @Override
    public Optional<Channel> getChannelByClientId(String clientId) {
        return Optional.ofNullable(clientIdToChannelMap.get(clientId));
    }

    @Override
    @GaugeIncMetric(name = METRIC_CONNECTION_ACTIVE)
    public void addChannel(String clientId, Channel channel) {
        clientIdToChannelMap.put(clientId, channel);
    }

    @Override
    @GaugeDecMetric(name = METRIC_CONNECTION_ACTIVE)
    public void closeChannel(Channel channel) {
        channel.closeFuture().addListener((ChannelFutureListener) future -> clientIdToChannelMap.remove(channel));
    }

    @Override
    public void putSession(Session session) {
        Optional<Session> oldSession = sessionRepository.findByBrokerIdAndClientId(session.getBrokerId(), session.getClientId());
        oldSession.ifPresent(s -> session.setId(s.getId()));
        sessionRepository.save(session);
    }

    @Override
    public Optional<Session> getSession(String brokerId, String clientId) {
        return sessionRepository.findByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public List<Session> getSession(String clientId) {
        return sessionRepository.findAllByClientId(clientId);
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
