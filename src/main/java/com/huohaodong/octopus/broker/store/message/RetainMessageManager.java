package com.huohaodong.octopus.broker.store.message;

import org.springframework.lang.Nullable;

import java.util.Collection;

public interface RetainMessageManager {

    void put(String topic, RetainMessage message);

    @Nullable
    RetainMessage get(String topic);

    @Nullable
    RetainMessage remove(String topic);

    boolean contains(String topic);

    @Nullable
    Collection<RetainMessage> getAllMatched(String topicFilter);

    int size();

}
