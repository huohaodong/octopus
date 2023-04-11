package com.huohaodong.octopus.broker.persistence.entity;

import com.huohaodong.octopus.broker.persistence.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import static com.huohaodong.octopus.broker.persistence.meta.Constants.*;

@Entity
@Data
@Table(
        name = "session",
        schema = "octopus",
        indexes = {
                @Index(name = "idx_broker_and_client", columnList = "broker_id, client_id")
        }
)
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", length = CLIENT_ID_LENGTH_MAX, nullable = false)
    private String clientId;

    @Column(name = "broker_id", length = BROKER_ID_LENGTH_MAX, nullable = false)
    private String brokerId;

    @Column(name = "client_ip", length = IP_LENGTH_MAX, nullable = false)
    private String clientIp;

    @Column(name = "broker_ip", length = IP_LENGTH_MAX, nullable = false)
    private String brokerIp;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private SessionStatus status;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false, nullable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
