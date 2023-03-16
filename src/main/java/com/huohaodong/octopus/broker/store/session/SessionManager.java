package com.huohaodong.octopus.broker.store.session;

import org.springframework.lang.Nullable;

public interface SessionManager {

    void put(String clientId, Session session);

    @Nullable
    Session get(String clientId);

    @Nullable
    Session remove(String clientId);

    boolean contains(String clientId);

}
