package com.huohaodong.octopus.broker.service.message;

import com.huohaodong.octopus.broker.persistence.entity.PublishMessage;
import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;

import java.util.List;
import java.util.Optional;

public interface PublishReleaseMessageManager {
    void putPublishReleaseMessage(PublishMessage publishMessage);

    Optional<PublishReleaseMessage> getPublishReleaseMessage(String brokerId, String clientId, Integer messageId);

    void removePublishReleaseMessage(String brokerId, String clientId, Integer messageId);

    List<PublishReleaseMessage> getAllPublishReleaseMessage(String brokerId, String clientId);

    void removeAllPublishReleaseMessage(String brokerId, String clientId);
}
