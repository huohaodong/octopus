package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.PublishMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishMessageRepository extends JpaRepository<PublishMessage, Long>, MessageBaseRepository<PublishMessage, Long> {
}
