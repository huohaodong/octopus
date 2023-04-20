package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.PublishMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishMessageRepository extends JpaRepository<PublishMessage, Long>, MessageBaseRepository<PublishMessage, Long> {
}
