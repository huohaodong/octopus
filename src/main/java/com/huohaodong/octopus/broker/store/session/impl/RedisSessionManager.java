package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.session.ChannelManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "spring.octopus.broker.storage.session", havingValue = "redis")
public class RedisSessionManager implements SessionManager, ChannelManager {

    private final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:SESSION:${spring.octopus.broker.id:DEFAULT_BROKER_ID}")
    private String SESSION_PREFIX;

    public RedisSessionManager(RedisConnectionFactory connectionFactory) {
        this.redisTemplate.setConnectionFactory(connectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        this.redisTemplate.setKeySerializer(stringRedisSerializer);
        this.redisTemplate.setHashKeySerializer(stringRedisSerializer);
        this.redisTemplate.afterPropertiesSet();
    }

    @Override
    public void put(String clientId, Session session) {
        redisTemplate.opsForHash().put(KEY(), clientId, session);
    }

    @Override
    public Session get(String clientId) {
        return (Session) redisTemplate.opsForHash().get(KEY(), clientId);
    }

    @Override
    public void remove(String clientId) {
        redisTemplate.opsForHash().delete(KEY(), clientId);
    }

    @Override
    public boolean contains(String clientId) {
        return redisTemplate.opsForHash().hasKey(KEY(), clientId);
    }

    @Override
    public void addChannel(Channel channel) {
        CHANNELS.add(channel);
    }

    @Override
    public Channel getChannel(String clientId) {
        Channel channel = null;
        Session session = get(clientId);
        if (session != null) {
            channel = CHANNELS.find(session.getChannelId());
        }
        return channel;
    }

    private String KEY() {
        return SESSION_PREFIX;
    }

}
