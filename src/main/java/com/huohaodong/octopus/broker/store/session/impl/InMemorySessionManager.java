package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import org.springframework.stereotype.Service;

@Service
public class InMemorySessionManager implements SessionManager {

    private final InMemoryRepository<String, Session> repository = new InMemoryRepository<>();

    @Override
    public Session put(String clientId, Session session) {
        return repository.put(clientId, session);
    }

    @Override
    public Session get(String clientId) {
        return repository.get(clientId);
    }

    @Override
    public Session remove(String clientId) {
        return repository.remove(clientId);
    }

    @Override
    public boolean containsKey(String clientId) {
        return repository.containsKey(clientId);
    }
}
