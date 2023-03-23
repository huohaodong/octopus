package com.huohaodong.octopus.broker.store.subscription.impl;

import com.google.gson.Gson;
import com.huohaodong.octopus.broker.server.metric.annotation.SubscribeMetric;
import com.huohaodong.octopus.broker.server.metric.annotation.TopicAddMetric;
import com.huohaodong.octopus.broker.server.metric.annotation.TopicRemoveMetric;
import com.huohaodong.octopus.broker.server.metric.annotation.UnSubscribeMetric;
import com.huohaodong.octopus.broker.server.metric.aspect.StatsCollector;
import com.huohaodong.octopus.broker.store.config.StoreConfig;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "spring.octopus.broker.storage.subscription", havingValue = "redis")
public class RedisSubscriptionManager implements SubscriptionManager {

    private final StoreConfig storeConfig;

    /* 每个 Topic 对应的所有符合条件的 Subscription (ClientId 和 Topic) */
    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();
    /*每个 clientId 对应的订阅信息 */
    private final RedisTemplate<String, Subscription> redisTemplate = new RedisTemplate<>();
    private final Set<String> TOPICS = new CopyOnWriteArraySet<>();

    private final StatsCollector statsCollector;

    private final Gson GSON = new Gson();

    public RedisSubscriptionManager(RedisConnectionFactory connectionFactory, StoreConfig storeConfig, StatsCollector statsCollector) {
        this.storeConfig = storeConfig;
        this.statsCollector = statsCollector;
        this.redisTemplate.setConnectionFactory(connectionFactory);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        this.redisTemplate.setDefaultSerializer(stringSerializer);
        this.redisTemplate.setEnableDefaultSerializer(true);
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    @SubscribeMetric
    public boolean subscribe(Subscription subscription) {
        if (subscription == null) {
            return false;
        }
        String clientId = subscription.getClientId();
        redisTemplate.opsForHash().put(KEY(clientId), subscription.getTopic(), GSON.toJson(subscription));
        statsCollector.getDeltaTotalTopics().accumulateAndGet(addTopic(subscription.getTopic()), Long::sum);
        return matcher.subscribe(subscription);
    }

    @Override
    @UnSubscribeMetric
    public boolean unSubscribe(String clientId, String topicFilter) {
        redisTemplate.opsForHash().delete(KEY(clientId), topicFilter);
        statsCollector.getDeltaTotalTopics().accumulateAndGet(removeTopic(topicFilter), Long::sum);
        return matcher.unSubscribe(clientId, topicFilter);
    }

    @Override
    public Collection<Subscription> getAllMatched(String topicFilter) {
        return matcher.match(topicFilter);
    }

    @Override
    public Collection<Subscription> getAllByClientId(String clientId) {
        return redisTemplate.opsForHash().values(KEY(clientId))
                .stream()
                .map(o -> GSON.fromJson((String) o, Subscription.class))
                .collect(Collectors.toList());
    }

    @Override
    public void unSubscribeAll(String clientId) {
        Collection<Subscription> subscriptions = getAllByClientId(clientId);
        if (subscriptions != null) {
            for (Subscription sub : subscriptions) {
                unSubscribe(sub);
            }
            redisTemplate.delete(KEY(clientId));
        }
    }

    @Override
    public long size() {
        return matcher.size();
    }

    private String KEY(String clientId) {
        return storeConfig.SUB_PREFIX + clientId;
    }

    @TopicAddMetric
    private int addTopic(String topic) {
        if (TOPICS.contains(topic)) {
            return 0;
        }
        TOPICS.add(topic);
        return 1;
    }

    @TopicRemoveMetric
    private int removeTopic(String topic) {
        if (!TOPICS.contains(topic)) {
            return 0;
        }
        TOPICS.remove(topic);
        return -1;
    }
}
