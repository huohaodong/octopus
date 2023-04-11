package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.WillMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WillMessageRepository extends JpaRepository<WillMessage, Long> {
}
