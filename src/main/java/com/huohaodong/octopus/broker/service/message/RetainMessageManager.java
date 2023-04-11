package com.huohaodong.octopus.broker.service.message;

import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;

import java.util.Optional;

public interface RetainMessageManager {
    void putRetainMessage(RetainMessage retainMessage);

    Optional<RetainMessage> getRetainMessage(String brokerId, String topic);

    void removeRetainMessage(String brokerId, String topic);
}
