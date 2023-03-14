package com.huohaodong.octopus.broker.store.message;

import java.util.List;

public interface PublishMessageManager {

    void put(String clientId, PublishMessage message);

    List<PublishMessage> get(String clientId);

    void remove(String clientId);

    void remove(String clientId, int messageId);

}
