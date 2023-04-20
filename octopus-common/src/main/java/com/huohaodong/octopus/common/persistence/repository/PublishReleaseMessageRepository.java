package com.huohaodong.octopus.common.persistence.repository;

import com.huohaodong.octopus.common.persistence.entity.PublishReleaseMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishReleaseMessageRepository extends JpaRepository<PublishReleaseMessage, Long>, MessageBaseRepository<PublishReleaseMessage, Long> {
}
