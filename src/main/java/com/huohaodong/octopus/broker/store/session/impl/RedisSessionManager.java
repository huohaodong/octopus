package com.huohaodong.octopus.broker.store.session.impl;

import com.huohaodong.octopus.broker.store.config.StoreConfig;
import com.huohaodong.octopus.broker.store.session.ChannelManager;
import com.huohaodong.octopus.broker.store.session.Session;
import com.huohaodong.octopus.broker.store.session.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "spring.octopus.broker.storage.session", havingValue = "redis")
public class RedisSessionManager implements SessionManager, ChannelManager {

    private final StoreConfig storeConfig;

    private final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

    public RedisSessionManager(RedisConnectionFactory connectionFactory, StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
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
        return storeConfig.SESSION_PREFIX;
    }

}
