package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;

public class InMemorySessionManager implements SessionManager {

    private final InMemoryRepository<String, Session> repo = new InMemoryRepository<>();

    @Override
    public Session put(String clientId, Session session) {
        return repo.put(clientId, session);
    }

    @Override
    public Session get(String clientId) {
        return repo.get(clientId);
    }

    @Override
    public Session remove(String clientId) {
        return repo.remove(clientId);
    }

    @Override
    public boolean contains(String clientId) {
        return repo.get(clientId) != null;
    }
}
