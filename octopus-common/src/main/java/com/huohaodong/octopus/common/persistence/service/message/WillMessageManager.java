package com.huohaodong.octopus.common.persistence.service.message;

import com.huohaodong.octopus.common.persistence.entity.WillMessage;

import java.util.Optional;

public interface WillMessageManager {
    void putWillMessage(WillMessage willMessage);

    Optional<WillMessage> getWillMessage(String brokerId, String clientId);

    void removeWillMessage(String brokerId, String clientId);
}
