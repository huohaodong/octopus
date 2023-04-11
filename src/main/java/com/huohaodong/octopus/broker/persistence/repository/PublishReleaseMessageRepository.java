package com.huohaodong.octopus.broker.persistence.repository;

import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishReleaseMessageRepository extends JpaRepository<PublishReleaseMessage, Long>, MessageBaseRepository<PublishReleaseMessage, Long> {
}
