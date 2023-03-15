package com.huohaodong.octopus.broker.store.message;

import java.util.Collection;

public interface PublishMessageManager {

    void put(String clientId, PublishMessage message);

    Collection<PublishMessage> getAllByClientId(String clientId);

    PublishMessage get(String clientId, int messageId);

    boolean remove(String clientId, int messageId);

    int removeAllByClientId(String clientId);

    int size();

    default void put(PublishMessage message) {
        put(message.getClientId(), message);
    }

}
