package com.huohaodong.octopus.common.persistence.service.session;

import com.huohaodong.octopus.common.persistence.entity.Session;

import java.util.Optional;

public interface SessionManager {
    void putSession(Session session);

    Optional<Session> getSession(String brokerId, String clientId);

    void removeSession(String brokerId, String clientId);

    boolean containsSession(String brokerId, String clientId);
}
