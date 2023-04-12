package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByBrokerIdAndClientIdAndTopic(String brokerId, String clientId, String topic);

    void deleteByBrokerIdAndClientIdAndTopic(String brokerId, String clientId, String topic);

    List<Subscription> findAllByBrokerIdAndClientId(String brokerId, String clientId);

    void deleteAllByBrokerIdAndClientId(String brokerId, String clientId);
}
