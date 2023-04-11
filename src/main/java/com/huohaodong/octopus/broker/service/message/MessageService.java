package com.huohaodong.octopus.broker.service.message;

public interface MessageService extends MessageIdManager, PublishMessageManager, PublishReleaseMessageManager, RetainMessageManager, WillMessageManager {
}
