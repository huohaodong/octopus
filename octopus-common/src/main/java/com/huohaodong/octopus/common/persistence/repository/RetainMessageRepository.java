package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.RetainMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetainMessageRepository extends JpaRepository<RetainMessage, Long> {
    Optional<RetainMessage> findByBrokerIdAndTopic(String brokerId, String topic);

    List<RetainMessage> findAllByBrokerId(String brokerId);

    void deleteByBrokerIdAndTopic(String brokerId, String topic);
}
