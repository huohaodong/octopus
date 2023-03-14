package com.huohaodong.octopus.broker.store.message;

import java.util.Set;

public interface RetainMessageManager {

    RetainMessage put(String topic, RetainMessage message);

    RetainMessage get(String topic);

    RetainMessage remove(String topic);

    boolean contains(String topic);

    Set<RetainMessage> getAllMatched(String topicFilter);

    int size();

}
