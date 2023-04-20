package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.WillMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WillMessageRepository extends JpaRepository<WillMessage, Long> {
    Optional<WillMessage> findByBrokerIdAndClientId(String brokerId, String clientId);

    void deleteByBrokerIdAndClientId(String brokerId, String clientId);

}
