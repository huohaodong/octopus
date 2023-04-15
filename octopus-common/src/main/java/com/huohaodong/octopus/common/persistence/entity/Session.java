package com.huohaodong.octopus.common.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "session",
        schema = "octopus",
        indexes = {
                @Index(name = "idx_broker_and_client", columnList = "broker_id, client_id")
        }
)
public class Session implements Serializable {
    @Serial
    private static final long serialVersionUID = 2656580891333983834L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", length = Constants.CLIENT_ID_LENGTH_MAX, nullable = false)
    private String clientId;

    @Column(name = "broker_id", length = Constants.BROKER_ID_LENGTH_MAX, nullable = false)
    private String brokerId;

    @Column(name = "client_ip", length = Constants.IP_LENGTH_MAX, nullable = false)
    private String clientIp;

    @Column(name = "broker_ip", length = Constants.IP_LENGTH_MAX, nullable = false)
    private String brokerIp;

    @Column(name = "clean_session", nullable = false)
    private boolean cleanSession;

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
