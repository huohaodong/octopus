package com.huohaodong.octopus.broker.persistence.entity;

import java.time.LocalDateTime;


public class Session {
    private Long id;

    private String clientId;

    private String clientIp;

    private String brokerId;

    private String brokerIp;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime lastOnlineTime;

    private SessionStatus status;

}
