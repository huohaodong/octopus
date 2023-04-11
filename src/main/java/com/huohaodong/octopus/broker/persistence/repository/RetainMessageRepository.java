package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RetainMessageRepository extends JpaRepository<RetainMessage, Long> {
    Optional<RetainMessage> findByBrokerIdAndTopic(String brokerId, String topic);

    void deleteByBrokerIdAndTopic(String brokerId, String topic);
}
