package com.huohaodong.octopus.broker.store.subscription.impl;

import com.google.gson.Gson;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionManager;
import com.huohaodong.octopus.broker.store.subscription.SubscriptionMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "spring.octopus.broker.storage.subscription", havingValue = "redis")
public class RedisSubscriptionManager implements SubscriptionManager {

    /* 每个 Topic 对应的所有符合条件的 Subscription (ClientId 和 Topic) */
    private final SubscriptionMatcher matcher = new CTrieSubscriptionMatcher();
    /*每个 clientId 对应的订阅信息 */
    private final RedisTemplate<String, Subscription> redisTemplate = new RedisTemplate<>();
    @Value("${spring.octopus.broker.id:DEFAULT_BROKER_ID}")
    private String PREFIX;

    private final Gson GSON = new Gson();

    public RedisSubscriptionManager(RedisConnectionFactory connectionFactory) {
        this.redisTemplate.setConnectionFactory(connectionFactory);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        this.redisTemplate.setDefaultSerializer(stringSerializer);
        this.redisTemplate.setEnableDefaultSerializer(true);
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    public boolean subscribe(Subscription subscription) {
        if (subscription == null) {
            return false;
        }
        String clientId = subscription.getClientId();
        redisTemplate.opsForHash().put(KEY(clientId), subscription.getTopic(), GSON.toJson(subscription));
        return matcher.subscribe(subscription);
    }

    @Override
    public boolean unSubscribe(String clientId, String topicFilter) {
        redisTemplate.opsForHash().delete(KEY(clientId), topicFilter);
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
        System.out.println(subscriptions);
        if (subscriptions != null) {
            for (Subscription sub : subscriptions) {
                unSubscribe(sub);
            }
            redisTemplate.delete(KEY(clientId));
        }
    }

    private String KEY(String clientId) {
        return PREFIX + ":" + clientId;
    }
}
