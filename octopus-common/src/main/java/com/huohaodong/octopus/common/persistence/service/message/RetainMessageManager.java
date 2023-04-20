package com.huohaodong.octopus.common.persistence.service.message;

import com.huohaodong.octopus.common.persistence.entity.RetainMessage;

import java.util.List;
import java.util.Optional;

public interface RetainMessageManager {
    void putRetainMessage(RetainMessage retainMessage);

    Optional<RetainMessage> getRetainMessage(String brokerId, String topic);

    List<RetainMessage> getAllRetainMessage(String brokerId);

    void removeRetainMessage(String brokerId, String topic);
}
