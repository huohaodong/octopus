package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InMemoryPublishMessageManager implements PublishMessageManager {

    /* ClientId -> Message */
    ConcurrentHashMap<String, List<PublishMessage>> map = new ConcurrentHashMap<>();

    DefaultMessageIdGenerator idGenerator;

    public InMemoryPublishMessageManager(DefaultMessageIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public void put(String clientId, PublishMessage message) {
        map.putIfAbsent(clientId, new CopyOnWriteArrayList<>());
        map.get(clientId).add(message);
    }

    @Override
    public Collection<PublishMessage> getAllByClientId(String clientId) {
        return map.get(clientId);
    }

    @Override
    public PublishMessage get(String clientId, int messageId) {
        if (!map.containsKey(clientId)) {
            return null;
        }
        for (PublishMessage message : map.get(clientId)) {
            if (message.getMessageId() == messageId) {
                return message;
            }
        }
        return null;
    }

    @Override
    public boolean remove(String clientId, int messageId) {
        if (!map.containsKey(clientId)) {
            return false;
        }
        idGenerator.releaseId(messageId);
        return map.get(clientId).removeIf(message -> message.getMessageId() == messageId);
    }

    @Override
    public int removeAllByClientId(String clientId) {
        if (!map.containsKey(clientId)) {
            return 0;
        }
        Collection<PublishMessage> messages = map.remove(clientId);
        if (messages != null) {
            messages.forEach(msg -> idGenerator.releaseId(msg.getMessageId()));
            return messages.size();
        }
        return 0;
    }

    @Override
    public int size() {
        return map.size();
    }
}
