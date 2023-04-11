package com.huohaodong.octopus.broker.service.message;

public interface MessageIdManager {
    int acquire(String clientId);

    void release(String clientId, int messageId);
}
