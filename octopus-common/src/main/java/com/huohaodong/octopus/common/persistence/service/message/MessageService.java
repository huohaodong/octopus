package com.huohaodong.octopus.common.persistence.service.message;

public interface MessageService extends MessageIdManager, PublishMessageManager, PublishReleaseMessageManager, RetainMessageManager, WillMessageManager {
}
