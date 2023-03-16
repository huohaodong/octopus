package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemorySessionManager implements SessionManager {

    private final ConcurrentHashMap<String, Session> map = new ConcurrentHashMap<>();

    @Override
    public Session put(String clientId, Session session) {
        return map.put(clientId, session);
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
}
