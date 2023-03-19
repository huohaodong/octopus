package com.huohaodong.octopus.broker.store.message.impl;

import com.google.gson.Gson;
import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.PublishMessageManager;
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
@ConditionalOnProperty(value = "spring.octopus.broker.storage.publish", havingValue = "redis")
public class RedisPublishMessageManager implements PublishMessageManager {

    private final RedisTemplate<String, PublishMessage> redisTemplate = new RedisTemplate<>();

    private final DefaultMessageIdGenerator idGenerator;

    private final Gson GSON = new Gson();

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:PUB:")
    private String PUB_PREFIX;

    public RedisPublishMessageManager(RedisConnectionFactory connectionFactory, DefaultMessageIdGenerator idGenerator) {
        this.redisTemplate.setConnectionFactory(connectionFactory);
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        this.redisTemplate.setDefaultSerializer(stringSerializer);
        this.redisTemplate.setEnableDefaultSerializer(true);
        this.redisTemplate.afterPropertiesSet();
        this.idGenerator = idGenerator;
    }

    @Override
    public void put(String clientId, PublishMessage message) {
        redisTemplate.opsForHash().put(KEY(clientId), String.valueOf(message.getMessageId()), GSON.toJson(message));
    }

    @Override
    public Collection<PublishMessage> getAllByClientId(String clientId) {
        return redisTemplate.opsForHash().values(KEY(clientId))
                .stream()
                .map(o -> GSON.fromJson((String) o, PublishMessage.class))
                .collect(Collectors.toList());
    }

    @Override
    public PublishMessage get(String clientId, int messageId) {
        return GSON.fromJson((String) redisTemplate.opsForHash().get(KEY(clientId), String.valueOf(messageId)), PublishMessage.class);
    }

    @Override
    public void remove(String clientId, int messageId) {
        redisTemplate.opsForHash().delete(KEY(clientId), String.valueOf(messageId));
    }

    @Override
    public int removeAllByClientId(String clientId) {
        Collection<PublishMessage> messages = getAllByClientId(clientId);
        if (messages != null && messages.size() > 0) {
            messages.forEach(msg -> idGenerator.releaseId(msg.getMessageId()));
            return Math.toIntExact(redisTemplate.opsForHash().delete(KEY(clientId)));
        }
        return 0;
    }

    @Override
    public int size() {
        return Math.toIntExact(redisTemplate.opsForHash().keys(PUB_PREFIX + "*").size());
    }

    private String KEY(String clientId) {
        return PUB_PREFIX + clientId;
    }
}
