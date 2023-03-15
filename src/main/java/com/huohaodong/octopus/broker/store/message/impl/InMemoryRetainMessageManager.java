package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessageManager;
import com.huohaodong.octopus.broker.store.persistent.Repository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InMemoryRetainMessageManager implements RetainMessageManager {

    private final Repository<String, RetainMessage> inMemoryRetainMessageRepository;

    public InMemoryRetainMessageManager(Repository<String, RetainMessage> inMemoryRetainMessageRepository) {
        this.inMemoryRetainMessageRepository = inMemoryRetainMessageRepository;
    }

    @Override
    public RetainMessage put(String topic, RetainMessage message) {
        return inMemoryRetainMessageRepository.put(topic, message);
    }

    @Override
    public RetainMessage get(String topic) {
        return inMemoryRetainMessageRepository.get(topic);
    }

    @Override
    public RetainMessage remove(String topic) {
        return inMemoryRetainMessageRepository.remove(topic);
    }

    @Override
    public boolean contains(String topic) {
        return inMemoryRetainMessageRepository.get(topic) != null;
    }

    @Override
    public Set<RetainMessage> getAllMatched(String topicFilter) {
        Set<RetainMessage> retainMessages = new HashSet<>();
        if (!topicFilter.contains("#") && !topicFilter.contains("+")) {
            if (inMemoryRetainMessageRepository.containsKey(topicFilter)) {
                retainMessages.add(inMemoryRetainMessageRepository.get(topicFilter));
            }
        } else {
            inMemoryRetainMessageRepository.getAll().forEach(entry -> {
                String topic = entry.getTopic();
                String[] splitTopics = topic.split("/");
                String[] splitTopicFilters = topicFilter.split("/");
                if (splitTopics.length >= splitTopicFilters.length) {
                    String newTopicFilter = "";
                    for (int i = 0; i < splitTopicFilters.length; i++) {
                        String value = splitTopicFilters[i];
                        if (value.equals("+")) {
                            newTopicFilter = newTopicFilter + "+/";
                        } else if (value.equals("#")) {
                            newTopicFilter = newTopicFilter + "#/";
                            break;
                        } else {
                            newTopicFilter = newTopicFilter + splitTopics[i] + "/";
                        }
                    }
                    newTopicFilter = newTopicFilter.substring(0, newTopicFilter.lastIndexOf("/"));
                    if (topicFilter.equals(newTopicFilter)) {
                        retainMessages.add(entry);
                    }
                }
            });
        }
        return retainMessages;
    }

    @Override
    public int size() {
        return inMemoryRetainMessageRepository.size();
    }
}
