package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessageManager;
import com.huohaodong.octopus.broker.store.persistent.Repository;

import java.util.Set;

public class InMemoryRetainMessageManager implements RetainMessageManager {

    private final Repository<String, RetainMessage> repository;

    public InMemoryRetainMessageManager(Repository<String, RetainMessage> repository) {
        this.repository = repository;
    }

    @Override
    public RetainMessage put(String topic, RetainMessage message) {
        return repository.put(topic, message);
    }

    @Override
    public RetainMessage get(String topic) {
        return repository.get(topic);
    }

    @Override
    public RetainMessage remove(String topic) {
        return repository.remove(topic);
    }

    @Override
    public boolean contains(String topic) {
        return repository.get(topic) != null;
    }

    // TODO 待实现
    @Override
    public Set<RetainMessage> getAllMatched(String topicFilter) {
        return null;
    }
}
