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
    private String group = "DEFAULT_BROKER_GROUP";
    private String host = "localhost";
    private int port = 20000;
    private String storage = "local";

    @Getter
    @Setter
    public static class Storage {
        private String session = "local";
        private String retain = "local";
        private String subscription = "local";
        private String publish = "local";
    }
}
