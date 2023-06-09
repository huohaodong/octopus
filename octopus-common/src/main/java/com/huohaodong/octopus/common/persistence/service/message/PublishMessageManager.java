package com.huohaodong.octopus.common.persistence.service.message;


import com.huohaodong.octopus.common.persistence.entity.PublishMessage;

import java.util.List;
import java.util.Optional;

public interface PublishMessageManager {
    void putPublishMessage(PublishMessage publishMessage);

    Optional<PublishMessage> getPublishMessage(String brokerId, String clientId, Integer messageId);

    void removePublishMessage(String brokerId, String clientId, Integer messageId);

    List<PublishMessage> getAllPublishMessage(String brokerId, String clientId);

    void removeAllPublishMessage(String brokerId, String clientId);

    List<PublishMessage> getAllPublishMessageByClientId(String clientId);

    void removeAllPublishMessageByClientId(String clientId);
}
