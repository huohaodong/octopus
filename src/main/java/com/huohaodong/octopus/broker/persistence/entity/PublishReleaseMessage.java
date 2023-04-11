package com.huohaodong.octopus.broker.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static com.huohaodong.octopus.broker.persistence.entity.Constants.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "publish_release_message",
        schema = "octopus",
        indexes = {
                @Index(name = "idx_message_identity", columnList = "broker_id, client_id, message_id"),
        }
)
public class PublishReleaseMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 4486458357608410789L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "broker_id", length = BROKER_ID_LENGTH_MAX, nullable = false)
    private String brokerId;

    @Column(name = "client_id", length = CLIENT_ID_LENGTH_MAX, nullable = false)
    private String clientId;

    @Column(name = "message_id", length = MESSAGE_ID_LENGTH_MAX, nullable = false)
    private Integer messageId;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false, nullable = false)
    private LocalDateTime createTime;
}
