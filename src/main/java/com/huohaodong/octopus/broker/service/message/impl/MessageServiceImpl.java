package com.huohaodong.octopus.broker.service.message.impl;

import com.huohaodong.octopus.broker.persistence.entity.PublishMessage;
import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;
import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;
import com.huohaodong.octopus.broker.persistence.entity.WillMessage;
import com.huohaodong.octopus.broker.persistence.repository.PublishMessageRepository;
import com.huohaodong.octopus.broker.persistence.repository.PublishReleaseMessageRepository;
import com.huohaodong.octopus.broker.persistence.repository.RetainMessageRepository;
import com.huohaodong.octopus.broker.persistence.repository.WillMessageRepository;
import com.huohaodong.octopus.broker.service.message.MessageService;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.*;

import static com.huohaodong.octopus.broker.service.cache.Constants.CACHE_RETAIN_MESSAGE;
import static com.huohaodong.octopus.broker.service.cache.Constants.CACHE_WILL_MESSAGE;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConcurrentReferenceHashMap<Channel, MessageIdGenerator> messageIdMap = new ConcurrentReferenceHashMap<>(32);

    private final PublishMessageRepository publishMessageRepository;

    private final PublishReleaseMessageRepository publishReleaseMessageRepository;

    private final RetainMessageRepository retainMessageRepository;

    private final WillMessageRepository willMessageRepository;

    @Override
    public int acquireNextMessageId(Channel channel) {
        messageIdMap.putIfAbsent(channel, new MessageIdGenerator());
        return Objects.requireNonNull(messageIdMap.get(channel)).acquire();
    }

    @Override
    public void releaseMessageId(Channel channel, int messageId) {
        if (messageIdMap.containsKey(channel)) {
            Objects.requireNonNull(messageIdMap.get(channel)).release(messageId);
        }
    }

    @Override
    public void putPublishMessage(PublishMessage publishMessage) {
        publishMessageRepository.save(publishMessage);
    }

    @Override
    public Optional<PublishMessage> getPublishMessage(String brokerId, String clientId, Integer messageId) {
        return publishMessageRepository.findByBrokerIdAndClientIdAndMessageId(brokerId, clientId, messageId);
    }

    @Override
    public void removePublishMessage(String brokerId, String clientId, Integer messageId) {
        publishMessageRepository.deleteByBrokerIdAndClientIdAndMessageId(brokerId, clientId, messageId);
    }

    @Override
    public List<PublishMessage> getAllPublishMessage(String brokerId, String clientId) {
        return publishMessageRepository.findAllByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public void removeAllPublishMessage(String brokerId, String clientId) {
        publishMessageRepository.deleteAllByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public List<PublishMessage> getAllPublishMessageByClientId(String clientId) {
        return publishMessageRepository.findAllByClientId(clientId);
    }

    @Override
    public void removeAllPublishMessageByClientId(String clientId) {
        publishMessageRepository.deleteAllByClientId(clientId);
    }

    @Override
    public void putPublishReleaseMessage(PublishReleaseMessage publishReleaseMessage) {
        publishReleaseMessageRepository.save(publishReleaseMessage);
    }

    @Override
    public Optional<PublishReleaseMessage> getPublishReleaseMessage(String brokerId, String clientId, Integer messageId) {
        return publishReleaseMessageRepository.findByBrokerIdAndClientIdAndMessageId(brokerId, clientId, messageId);
    }

    @Override
    public void removePublishReleaseMessage(String brokerId, String clientId, Integer messageId) {
        publishReleaseMessageRepository.deleteByBrokerIdAndClientIdAndMessageId(brokerId, clientId, messageId);
    }

    @Override
    public List<PublishReleaseMessage> getAllPublishReleaseMessage(String brokerId, String clientId) {
        return publishReleaseMessageRepository.findAllByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public void removeAllPublishReleaseMessage(String brokerId, String clientId) {
        publishReleaseMessageRepository.deleteAllByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    public List<PublishReleaseMessage> getAllPublishReleaseMessageByClientId(String clientId) {
        return publishReleaseMessageRepository.findAllByClientId(clientId);
    }

    @Override
    public void removeAllPublishReleaseMessageByClientId(String clientId) {
        publishReleaseMessageRepository.deleteAllByClientId(clientId);
    }

    @Override
    @CachePut(value = CACHE_RETAIN_MESSAGE, key = "{#retainMessage.brokerId, #retainMessage.topic}")
    public void putRetainMessage(RetainMessage retainMessage) {
        Optional<RetainMessage> oldRetainMessage = retainMessageRepository.findByBrokerIdAndTopic(retainMessage.getBrokerId(), retainMessage.getTopic());
        oldRetainMessage.ifPresent(message -> retainMessage.setId(message.getId()));
        retainMessageRepository.save(retainMessage);
    }

    @Override
    @Cacheable(value = CACHE_RETAIN_MESSAGE, key = "{#brokerId, #topic}")
    public Optional<RetainMessage> getRetainMessage(String brokerId, String topic) {
        return retainMessageRepository.findByBrokerIdAndTopic(brokerId, topic);
    }

    @Override
    @Cacheable(value = CACHE_RETAIN_MESSAGE, key = "#brokerId")
    public List<RetainMessage> getAllRetainMessage(String brokerId) {
        return retainMessageRepository.findAllByBrokerId(brokerId);
    }

    @Override
    @CacheEvict(value = CACHE_RETAIN_MESSAGE, key = "{#brokerId, #topic}")
    public void removeRetainMessage(String brokerId, String topic) {
        retainMessageRepository.deleteByBrokerIdAndTopic(brokerId, topic);
    }

    @Override
    @CachePut(value = CACHE_WILL_MESSAGE, key = "{#willMessage.brokerId, #willMessage.clientId}")
    public void putWillMessage(WillMessage willMessage) {
        Optional<WillMessage> oldWillMessage = willMessageRepository.findByBrokerIdAndClientId(willMessage.getBrokerId(), willMessage.getClientId());
        oldWillMessage.ifPresent(message -> willMessage.setId(message.getId()));
        willMessageRepository.save(willMessage);
    }

    @Override
    @Cacheable(value = CACHE_WILL_MESSAGE, key = "{#brokerId, #clientId}")
    public Optional<WillMessage> getWillMessage(String brokerId, String clientId) {
        return willMessageRepository.findByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
    @CacheEvict(value = CACHE_WILL_MESSAGE, key = "{#brokerId, #clientId}")
    public void removeWillMessage(String brokerId, String clientId) {
        willMessageRepository.deleteByBrokerIdAndClientId(brokerId, clientId);
    }

    private static class MessageIdGenerator {

        private final int ID_MIN = 1;

        private final int ID_MAX = 65536;

        private final Set<Integer> ID_POOL = new HashSet<>(List.of(0));

        private int CANDIDATE = ID_MIN;

        public synchronized int acquire() {
            while (ID_POOL.contains(CANDIDATE)) {
                CANDIDATE = (CANDIDATE + 1) % ID_MAX;
            }
            ID_POOL.add(CANDIDATE);
            return CANDIDATE;
        }

        public synchronized void release(int messageId) {
            ID_POOL.remove(messageId);
        }
    }
}
