package com.huohaodong.octopus.broker.server.cluster.impl;

import com.huohaodong.octopus.broker.server.cluster.ClusterEventManager;
import com.huohaodong.octopus.broker.server.cluster.ClusterMessageIdentity;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class RedisClusterEventManager extends MessageListenerAdapter implements ClusterEventManager {

    StringRedisTemplate redisTemplate;

    public RedisClusterEventManager(RedisConnectionFactory connectionFactory) {
        this.redisTemplate.setConnectionFactory(connectionFactory);
        this.redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {

    }

    @Override
    public void broadcast(ClusterMessageIdentity identity, MqttMessage message) {
        redisTemplate.convertAndSend("", message);
    }

    @Override
    public void closeByClientId(ClusterMessageIdentity identity, String ClientId) {

    }
}
