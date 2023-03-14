package com.huohaodong.octopus.broker.store.message;

import com.huohaodong.octopus.broker.store.session.Session;

public interface MessageManager {
    Session put(String clientId, Session session);

    Session get(String clientId);

    Session remove(String clientId);

    boolean contains(String clientId);
}
