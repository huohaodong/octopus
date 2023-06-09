package com.huohaodong.octopus.common.persistence.service.message;

import com.huohaodong.octopus.common.persistence.entity.PublishReleaseMessage;

import java.util.List;
import java.util.Optional;

public interface PublishReleaseMessageManager {
    void putPublishReleaseMessage(PublishReleaseMessage publishReleaseMessage);

    Optional<PublishReleaseMessage> getPublishReleaseMessage(String brokerId, String clientId, Integer messageId);

    void removePublishReleaseMessage(String brokerId, String clientId, Integer messageId);

    List<PublishReleaseMessage> getAllPublishReleaseMessage(String brokerId, String clientId);

    void removeAllPublishReleaseMessage(String brokerId, String clientId);

    List<PublishReleaseMessage> getAllPublishReleaseMessageByClientId(String clientId);

    void removeAllPublishReleaseMessageByClientId(String clientId);
}
