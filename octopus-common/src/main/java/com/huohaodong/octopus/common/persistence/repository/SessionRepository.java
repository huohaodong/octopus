package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByBrokerIdAndClientId(String brokerId, String clientId);

    List<Session> findAllByClientId(String clientId);

    void deleteByBrokerIdAndClientId(String brokerId, String clientId);

    boolean existsByBrokerIdAndClientId(String brokerId, String clientId);
}
