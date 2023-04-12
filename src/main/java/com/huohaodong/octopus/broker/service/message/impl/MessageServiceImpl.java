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
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConcurrentReferenceHashMap<Channel, MessageIdGenerator> messageIdMap = new ConcurrentReferenceHashMap<>(10, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    private final PublishMessageRepository publishMessageRepository;

    private final PublishReleaseMessageRepository publishReleaseMessageRepository;

    private final RetainMessageRepository retainMessageRepository;

    private final WillMessageRepository willMessageRepository;

    @Override
    public int acquireNextMessageId(Channel channel) {
        messageIdMap.putIfAbsent(channel, new MessageIdGenerator());
        return messageIdMap.get(channel).acquire();
    }

    @Override
    public void releaseMessageId(Channel channel, int messageId) {
        if (messageIdMap.containsKey(channel)) {
            messageIdMap.get(channel).release(messageId);
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
    public void putRetainMessage(RetainMessage retainMessage) {
        retainMessageRepository.save(retainMessage);
    }

    @Override
    public Optional<RetainMessage> getRetainMessage(String brokerId, String topic) {
        return retainMessageRepository.findByBrokerIdAndTopic(brokerId, topic);
    }

    @Override
    public void removeRetainMessage(String brokerId, String topic) {
        retainMessageRepository.deleteByBrokerIdAndTopic(brokerId, topic);
    }

    @Override
    public void putWillMessage(WillMessage willMessage) {
        willMessageRepository.save(willMessage);
    }

    @Override
    public Optional<WillMessage> getWillMessage(String brokerId, String clientId) {
        return willMessageRepository.findByBrokerIdAndClientId(brokerId, clientId);
    }

    @Override
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
