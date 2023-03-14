package com.huohaodong.octopus.broker.store.message;

import java.util.List;

public interface PublishMessageManager {

    PublishMessage put(String clientId, PublishMessage message);

    List<PublishMessage> get(String clientId);

    PublishMessage remove(String clientId);

    boolean remove(String clientId, int messageId);

    int size();

}
