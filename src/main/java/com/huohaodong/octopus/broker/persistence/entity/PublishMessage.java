package com.huohaodong.octopus.broker.persistence.entity;

import io.netty.handler.codec.mqtt.MqttQoS;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static com.huohaodong.octopus.broker.persistence.entity.Constants.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "publish_message",
        schema = "octopus",
        indexes = {
                @Index(name = "idx_message_identity", columnList = "broker_id, client_id, message_id"),
        }
)
public class PublishMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broker_id", length = BROKER_ID_LENGTH_MAX, nullable = false)
    private String brokerId;

    @Column(name = "client_id", length = CLIENT_ID_LENGTH_MAX, nullable = false)
    private String clientId;

    @Column(name = "message_id", length = MESSAGE_ID_LENGTH_MAX, nullable = false)
    private Integer messageId;

    @Column(name = "topic", length = TOPIC_LENGTH_MAX, nullable = false)
    private String topic;

    @Lob
    @Column(name = "payload", length = MESSAGE_PAYLOAD_LENGTH_MAX)
    private byte[] payload;

    @Column(name = "qos", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private MqttQoS qos;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false, nullable = false)
    private LocalDateTime createTime;
}
