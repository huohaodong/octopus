package com.huohaodong.octopus.broker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.octopus.broker")
public class BrokerConfig {
    private String id = "DEFAULT_BROKER_ID";
    private String host = "localhost";
    private int port = 20000;
}
