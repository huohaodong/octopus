package com.huohaodong.octopus.broker.store.message;

import org.springframework.lang.Nullable;

import java.util.Collection;

public interface PublishMessageManager {

    void put(String clientId, PublishMessage message);

    @Nullable
    Collection<PublishMessage> getAllByClientId(String clientId);

    @Nullable
    PublishMessage get(String clientId, int messageId);

    void remove(String clientId, int messageId);

    int removeAllByClientId(String clientId);

    int size();

    default void put(PublishMessage message) {
        put(message.getClientId(), message);
    }

}
