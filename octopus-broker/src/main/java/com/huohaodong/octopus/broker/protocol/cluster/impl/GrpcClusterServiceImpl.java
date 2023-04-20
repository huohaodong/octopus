package com.huohaodong.octopus.broker.protocol.cluster.impl;

import com.google.protobuf.ByteString;
import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.common.persistence.entity.PublishMessage;
import com.huohaodong.octopus.common.protocol.cluster.ClusterService;
import com.huohaodong.octopus.common.protocol.grpc.ClusterCloseChannelRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterEventListenerGrpc;
import com.huohaodong.octopus.common.protocol.grpc.ClusterPublishRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterRequest;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j(topic = "CLUSTER_SERVICE")
@RequiredArgsConstructor
@Service
public class GrpcClusterServiceImpl implements ClusterService {

    private final BrokerProperties brokerProperties;

    private final DiscoveryClient discoveryClient;
    private final String SERVICE_ID = "grpc-octopus-broker";
    private final String META_BROKER_ID = "broker-id";
    private final Map<String, ClusterGrpcClient> grpcClientMap = new ConcurrentHashMap<>();

    @Override
    public void broadcastPublishMessage(PublishMessage publishMessage) {
        ClusterPublishRequest publishRequest = buildPublishRequest(publishMessage);
        String localBrokerId = brokerProperties.getId();
        getRemoteGrpcServices().forEach(service -> {
            String remoteBrokerId = service.getMetadata().get(META_BROKER_ID);
            grpcClientMap.get(remoteBrokerId).asyncStub.onEvent(
                    ClusterRequest.newBuilder()
                            .setBrokerId(localBrokerId)
                            .setPublishRequest(publishRequest)
                            .build()
            );
        });
    }

    @Override
    public void broadcastToClose(String clientId) {
        String localBrokerId = brokerProperties.getId();
        getRemoteGrpcServices().forEach(service -> {
            String remoteBrokerId = service.getMetadata().get(META_BROKER_ID);
            grpcClientMap.get(remoteBrokerId).blockingStub.onEvent(
                    ClusterRequest.newBuilder()
                            .setBrokerId(localBrokerId)
                            .setCloseChannelRequest(ClusterCloseChannelRequest.newBuilder().setClientId(clientId).build())
                            .build()
            );
        });
    }

    private List<ServiceInstance> getRemoteGrpcServices() {
        return discoveryClient.getInstances(SERVICE_ID).stream().filter(serviceInstance -> {
            String remoteBrokerId = serviceInstance.getMetadata().get(META_BROKER_ID);
            if (remoteBrokerId == null) {
                log.error("Grpc meta data for broker id is not set, unable to broadcast publish message!");
                return false;
            }
            if (remoteBrokerId.equals(brokerProperties.getId())) {
                return false;
            }
            grpcClientMap.putIfAbsent(remoteBrokerId, new ClusterGrpcClient(
                    ManagedChannelBuilder.forAddress(serviceInstance.getHost(), serviceInstance.getPort()))
            );
            return true;
        }).collect(Collectors.toList());
    }

    private ClusterPublishRequest buildPublishRequest(PublishMessage publishMessage) {
        return ClusterPublishRequest.newBuilder()
                .setTopic(publishMessage.getTopic())
                .setPayload(ByteString.copyFrom(publishMessage.getPayload()))
                .setQos(ClusterPublishRequest.QoS.forNumber(publishMessage.getQos().value()))
                .build();
    }

    private static class ClusterGrpcClient {

        public ClusterEventListenerGrpc.ClusterEventListenerBlockingStub blockingStub;

        public ClusterEventListenerGrpc.ClusterEventListenerFutureStub asyncStub;

        public ClusterGrpcClient(Channel channel) {
            blockingStub = ClusterEventListenerGrpc.newBlockingStub(channel);
            asyncStub = ClusterEventListenerGrpc.newFutureStub(channel);
        }

        public ClusterGrpcClient(ManagedChannelBuilder<?> channelBuilder) {
            this(channelBuilder.usePlaintext().build());
        }
    }
}
