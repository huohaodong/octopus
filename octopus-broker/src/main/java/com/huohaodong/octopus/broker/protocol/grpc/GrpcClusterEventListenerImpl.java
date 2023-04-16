package com.huohaodong.octopus.broker.protocol.grpc;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.common.persistence.entity.PublishMessage;
import com.huohaodong.octopus.common.protocol.cluster.exception.UnknownClusterEventException;
import com.huohaodong.octopus.common.protocol.cluster.handler.ClusterEventHandler;
import com.huohaodong.octopus.common.protocol.grpc.*;
import com.huohaodong.octopus.common.protocol.grpc.ClusterCloseChannelRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterEventListenerGrpc;
import com.huohaodong.octopus.common.protocol.grpc.ClusterPublishRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterResponse;
import io.grpc.stub.StreamObserver;
import io.netty.handler.codec.mqtt.MqttQoS;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@Slf4j(topic = "CLUSTER_EVENT_LISTENER")
@GRpcService
@RequiredArgsConstructor
public class GrpcClusterEventListenerImpl extends ClusterEventListenerGrpc.ClusterEventListenerImplBase {

    private final BrokerProperties brokerProperties;

    private final ClusterEventHandler clusterEventHandler;

    private ClusterResponse CLUSTER_RESPONSE_OK;

    @PostConstruct
    private void init() {
        CLUSTER_RESPONSE_OK = ClusterResponse
                .newBuilder()
                .setBrokerId(brokerProperties.getId())
                .setResultCode(ClusterResponse.ResultCode.OK)
                .build();
    }

    @Override
    public void onEvent(ClusterRequest request, StreamObserver<ClusterResponse> responseObserver) {
        switch (request.getRequestCase()) {
            case PUBLISH_REQUEST -> {
                log.info("Received PUBLISH_REQUEST from broker {}", request.getBrokerId());
                ClusterPublishRequest publishRequest = request.getPublishRequest();
                clusterEventHandler.doPublish(
                        PublishMessage.builder()
                                .topic(publishRequest.getTopic())
                                .qos(MqttQoS.valueOf(publishRequest.getQos().getNumber()))
                                .payload(publishRequest.getPayload().toByteArray())
                                .build()
                );
                responseObserver.onNext(CLUSTER_RESPONSE_OK);
                responseObserver.onCompleted();
            }
            case CLOSE_CHANNEL_REQUEST -> {
                log.info("Received CLOSE_CHANNEL_REQUEST from broker {}", request.getBrokerId());
                ClusterCloseChannelRequest closeChannelRequest = request.getCloseChannelRequest();
                clusterEventHandler.doCloseChannel(closeChannelRequest.getClientId());
                responseObserver.onNext(CLUSTER_RESPONSE_OK);
                responseObserver.onCompleted();
            }
            case REQUEST_NOT_SET -> {
                log.warn("Unknown cluster event from broker {}", request.getBrokerId());
                responseObserver.onError(new UnknownClusterEventException());
            }
        }
    }
}
