package com.huohaodong.octopus.broker.store.message.impl;

import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessageManager;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRetainMessageManager implements RetainMessageManager {

    private final ConcurrentHashMap<String, RetainMessage> map = new ConcurrentHashMap<>();

    @Override
    public RetainMessage put(String topic, RetainMessage message) {
        return map.put(topic, message);
    }

    @Override
    public RetainMessage get(String topic) {
        return map.get(topic);
    }

    @Override
    public RetainMessage remove(String topic) {
        return map.remove(topic);
    }

    @Override
    public boolean contains(String topic) {
        return map.containsKey(topic);
    }

    @Override
    public Set<RetainMessage> getAllMatched(String topicFilter) {
        Set<RetainMessage> retainMessages = new HashSet<>();
        if (!topicFilter.contains("#") && !topicFilter.contains("+")) {
            if (map.containsKey(topicFilter)) {
                retainMessages.add(map.get(topicFilter));
            }
        } else {
            map.forEach((topic, retainMessage) -> {
                {
                    String[] splitTopics = topic.split("/");
                    String[] splitTopicFilters = topicFilter.split("/");
                    if (splitTopics.length >= splitTopicFilters.length) {
                        StringBuilder newTopicFilter = new StringBuilder();
                        for (int i = 0; i < splitTopicFilters.length; i++) {
                            String value = splitTopicFilters[i];
                            if (value.equals("+")) {
                                newTopicFilter.append("+/");
                            } else if (value.equals("#")) {
                                newTopicFilter.append("#/");
                                break;
                            } else {
                                newTopicFilter.append(splitTopics[i]).append("/");
                            }
                        }
                        newTopicFilter = new StringBuilder(newTopicFilter.substring(0, newTopicFilter.lastIndexOf("/")));
                        if (topicFilter.contentEquals(newTopicFilter)) {
                            retainMessages.add(retainMessage);
                        }
                    }
                }
            });
        }
        return retainMessages;
    }

    @Override
    public int size() {
        return map.size();
    }
}
