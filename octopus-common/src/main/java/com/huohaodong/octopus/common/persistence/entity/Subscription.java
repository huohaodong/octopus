package com.huohaodong.octopus.common.persistence.entity;

import io.netty.handler.codec.mqtt.MqttQoS;
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
        name = "subscription",
        schema = "octopus",
        indexes = {
                @Index(name = "idx_subscription_identity", columnList = "broker_id, client_id, topic")
        }
)
public class Subscription implements Serializable {
    @Serial
    private static final long serialVersionUID = 3780025551583217870L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broker_id", length = Constants.BROKER_ID_LENGTH_MAX, nullable = false)
    private String brokerId;

    @Column(name = "client_id", length = Constants.CLIENT_ID_LENGTH_MAX, nullable = false)
    private String clientId;

    @Column(name = "topic", length = Constants.TOPIC_LENGTH_MAX, nullable = false)
    private String topic;

    @Column(name = "qos", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private MqttQoS qos;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false, nullable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    public Subscription(String clientId, String topic) {
        this.clientId = clientId;
        this.topic = topic;
    }

    public Subscription(String clientId, String topic, MqttQoS qos) {
        this.clientId = clientId;
        this.topic = topic;
        this.qos = qos;
    }
}
