package com.huohaodong.octopus.broker.service.message.impl;

import com.huohaodong.octopus.broker.persistence.entity.PublishMessage;
import com.huohaodong.octopus.broker.persistence.entity.PublishReleaseMessage;
import com.huohaodong.octopus.broker.persistence.entity.RetainMessage;
import com.huohaodong.octopus.broker.persistence.entity.WillMessage;
import com.huohaodong.octopus.broker.service.message.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public int acquire(String clientId) {
        return 0;
    }

    @Override
    public void release(String clientId, int messageId) {

    }

    @Override
    public void putPublishMessage(PublishMessage publishMessage) {

    }

    @Override
    public Optional<PublishMessage> getPublishMessage(String brokerId, String clientId, Integer messageId) {
        return Optional.empty();
    }

    @Override
    public void removePublishMessage(String brokerId, String clientId, Integer messageId) {

    }

    @Override
    public List<PublishMessage> getAllPublishMessage(String brokerId, String clientId) {
        return null;
    }

    @Override
    public void removeAllPublishMessage(String brokerId, String clientId) {

    }

    @Override
    public void putPublishReleaseMessage(PublishMessage publishMessage) {

    }

    @Override
    public Optional<PublishReleaseMessage> getPublishReleaseMessage(String brokerId, String clientId, Integer messageId) {
        return Optional.empty();
    }

    @Override
    public void removePublishReleaseMessage(String brokerId, String clientId, Integer messageId) {

    }

    @Override
    public List<PublishReleaseMessage> getAllPublishReleaseMessage(String brokerId, String clientId) {
        return null;
    }

    @Override
    public void removeAllPublishReleaseMessage(String brokerId, String clientId) {

    }

    @Override
    public void putRetainMessage(RetainMessage retainMessage) {

    }

    @Override
    public Optional<RetainMessage> getRetainMessage(String brokerId, String topic) {
        return Optional.empty();
    }

    @Override
    public void removeRetainMessage(String brokerId, String topic) {

    }

    @Override
    public void putWillMessage(WillMessage willMessage) {

    }

    @Override
    public Optional<WillMessage> getWillMessage(String brokerId, String clientId) {
        return Optional.empty();
    }

    @Override
    public void removeWillMessage(String brokerId, String clientId) {

    }
}
