package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.persistent.Repository;

import java.util.List;
import java.util.stream.Collectors;

public class InMemoryPublishMessageManager implements PublishMessageManager {

    Repository<String, PublishMessage> repository;

    public InMemoryPublishMessageManager(Repository<String, PublishMessage> repository) {
        this.repository = repository;
    }

    @Override
    public PublishMessage put(String clientId, PublishMessage message) {
        return repository.put(clientId, message);
    }

    @Override
    public List<PublishMessage> get(String clientId) {
        return repository.getAll()
                .stream()
                .filter(message -> message.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    @Override
    public PublishMessage remove(String clientId) {
        return repository.remove(clientId);
    }

    @Override
    public boolean remove(String clientId, int messageId) {
        return repository.getAll()
                .removeIf(message -> message.getClientId().equals(clientId)
                        && message.getMessageId() == messageId);
    }

    @Override
    public int size() {
        return repository.size();
    }
}
