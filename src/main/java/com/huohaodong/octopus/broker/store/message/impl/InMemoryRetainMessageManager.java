package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessageManager;
import com.huohaodong.octopus.broker.store.persistent.Repository;

import java.util.HashSet;
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

    @Override
    public Set<RetainMessage> getAllMatched(String topicFilter) {
        Set<RetainMessage> retainMessages = new HashSet<>();
        if (!topicFilter.contains("#") && !topicFilter.contains("+")) {
            if (repository.containsKey(topicFilter)) {
                retainMessages.add(repository.get(topicFilter));
            }
        } else {
            repository.getAll().forEach(entry -> {
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
        return repository.size();
    }
}
