package com.huohaodong.octopus.broker.store.config;

import com.huohaodong.octopus.broker.store.message.PublishMessage;
import com.huohaodong.octopus.broker.store.message.RetainMessage;
import com.huohaodong.octopus.broker.store.persistent.Repository;
import com.huohaodong.octopus.broker.store.persistent.impl.InMemoryRepository;
import com.huohaodong.octopus.broker.store.subscription.Subscription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class StoreConfig {

    @Bean
    public InMemoryRepository<String, RetainMessage> inMemoryRetainMessageRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public InMemoryRepository<String, List<PublishMessage>> inMemoryPublishMessageRepository() {
        return new InMemoryRepository<>();
    }

    @Bean
    public Repository<String, List<Subscription>> inMemorySubscriptionRepository() {
        return new InMemoryRepository<>();
    }
}
