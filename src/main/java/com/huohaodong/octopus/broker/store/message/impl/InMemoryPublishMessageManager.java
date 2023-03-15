package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InMemoryPublishMessageManager implements PublishMessageManager {

    /* ClientId -> Message */
    InMemoryRepository<String, List<PublishMessage>> inMemoryPublishMessageRepository;

    DefaultMessageIdGenerator idGenerator;

    public InMemoryPublishMessageManager(InMemoryRepository<String, List<PublishMessage>> inMemoryPublishMessageRepository, DefaultMessageIdGenerator idGenerator) {
        this.inMemoryPublishMessageRepository = inMemoryPublishMessageRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public void put(String clientId, PublishMessage message) {
        if (!inMemoryPublishMessageRepository.containsKey(clientId)) {
            inMemoryPublishMessageRepository.put(clientId, new CopyOnWriteArrayList<>());
        }
        inMemoryPublishMessageRepository.get(clientId).add(message);
    }

    @Override
    public Collection<PublishMessage> getAllByClientId(String clientId) {
        return inMemoryPublishMessageRepository.get(clientId);
    }

    @Override
    public PublishMessage get(String clientId, int messageId) {
        if (!inMemoryPublishMessageRepository.containsKey(clientId)) {
            return null;
        }
        for (PublishMessage message : inMemoryPublishMessageRepository.get(clientId)) {
            if (message.getMessageId() == messageId) {
                return message;
            }
        }
        return null;
    }

    @Override
    public boolean remove(String clientId, int messageId) {
        if (!inMemoryPublishMessageRepository.containsKey(clientId)) {
            return false;
        }
        idGenerator.releaseId(messageId);
        return inMemoryPublishMessageRepository.get(clientId).removeIf(message -> message.getMessageId() == messageId);
    }

    @Override
    public int removeAllByClientId(String clientId) {
        if (!inMemoryPublishMessageRepository.containsKey(clientId)) {
            return 0;
        }
        Collection<PublishMessage> messages = inMemoryPublishMessageRepository.remove(clientId);
        if (messages != null) {
            messages.forEach(msg -> idGenerator.releaseId(msg.getMessageId()));
            return messages.size();
        }
        return 0;
    }

    @Override
    public int size() {
        return inMemoryPublishMessageRepository.size();
    }
}
