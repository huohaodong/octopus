package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByBrokerIdAndClientIdAndTopic(String brokerId, String clientId, String topic);

    void deleteByBrokerIdAndClientIdAndTopic(String brokerId, String clientId, String topic);

    List<Subscription> findAllByBrokerIdAndClientId(String brokerId, String clientId);

    void deleteAllByBrokerIdAndClientId(String brokerId, String clientId);

    boolean existsByBrokerIdAndClientIdAndTopic(String brokerId, String clientId, String topic);
}
