package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;

public class RedisSessionManager implements SessionManager {
    @Override
    public void put(String clientId, Session session) {

    }

    @Override
    public Session get(String clientId) {
        return null;
    }

    @Override
    public Session remove(String clientId) {
        return null;
    }

    @Override
    public boolean contains(String clientId) {
        return false;
    }
}
