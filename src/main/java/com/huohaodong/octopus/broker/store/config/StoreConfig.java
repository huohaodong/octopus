package com.huohaodong.octopus.broker.store.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
public class StoreConfig {

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:PUB:")
    public String PUB_PREFIX;

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:SESSION:${spring.octopus.broker.id:DEFAULT_BROKER_ID}")
    public String SESSION_PREFIX;

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:SUB:")
    public String SUB_PREFIX;

    @Value("${spring.octopus.broker.group:DEFAULT_BROKER_GROUP}:RETAIN")
    public String RETAIN_PREFIX;

}
