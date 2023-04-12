package com.huohaodong.octopus.broker.persistence.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface MessageBaseRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findByBrokerIdAndClientIdAndMessageId(String brokerId, String clientId, Integer messageId);

    void deleteByBrokerIdAndClientIdAndMessageId(String brokerId, String clientId, Integer messageId);

    List<T> findAllByBrokerIdAndClientId(String brokerId, String clientId);

    void deleteAllByBrokerIdAndClientId(String brokerId, String clientId);

    boolean existsByBrokerIdAndClientIdAndMessageId(String brokerId, String clientId, Integer messageId);
}
