package com.huohaodong.octopus.broker.store.session;

public interface SessionManager {

    Session put(String clientId, Session session);

    Session get(String clientId);

    Session remove(String clientId);

    boolean contains(String clientId);

}
