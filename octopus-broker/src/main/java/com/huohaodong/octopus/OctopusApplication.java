package com.huohaodong.octopus;

import com.huohaodong.octopus.common.protocol.grpc.ClusterCloseChannelRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterEventListenerGrpc;
import com.huohaodong.octopus.common.protocol.grpc.ClusterRequest;
import com.huohaodong.octopus.common.protocol.grpc.ClusterResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.function.Consumer;

@SpringBootApplication(scanBasePackages = "com.huohaodong.octopus")
@EnableTransactionManagement
@EnableJpaRepositories
@EnableCaching
@EnableDiscoveryClient
public class OctopusApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(OctopusApplication.class, args);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                DiscoveryClient discoveryClient = ctx.getBean(DiscoveryClient.class);
                System.out.println(discoveryClient.getServices());
                List<ServiceInstance> serviceInstances = discoveryClient.getInstances("grpc-octopus-broker");
                serviceInstances.forEach(serviceInstance -> {
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(serviceInstance.getHost(),
                                    serviceInstance.getPort())
                            .usePlaintext()
                            .build();
                    ClusterCloseChannelRequest request = ClusterCloseChannelRequest.newBuilder().setClientId("client1").build();
                    ClusterResponse response = ClusterEventListenerGrpc.newBlockingStub(channel).onEvent(
                            ClusterRequest.newBuilder().setBrokerId("BROKER2").setCloseChannelRequest(request).build()
                    );
                    System.out.println(response);
                });
            }
        }).start();
    }
}
