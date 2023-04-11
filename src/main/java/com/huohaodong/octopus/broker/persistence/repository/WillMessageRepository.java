package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.WillMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WillMessageRepository extends JpaRepository<WillMessage, Long> {
    Optional<WillMessage> findByBrokerIdAndClientId(String brokerId, String clientId);

    void deleteByBrokerIdAndClientId(String brokerId, String clientId);

}
