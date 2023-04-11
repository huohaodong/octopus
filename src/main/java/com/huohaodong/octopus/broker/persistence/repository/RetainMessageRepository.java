package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetainMessageRepository extends JpaRepository<RetainMessage, Long> {
}
