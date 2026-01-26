package com.fivontwov.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fivontwov.user.proto.UserServiceGrpc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
public class GrpcConfig {

    private final DiscoveryClient discoveryClient;
    private final AtomicInteger counter = new AtomicInteger();

    public GrpcConfig(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel userChannel() {
        List<ServiceInstance> instances = discoveryClient.getInstances("STUDY-MANAGEMENT-SERVICE");
        if (instances.isEmpty()) {
            throw new IllegalStateException("No instances of DDP-STUDY-MANAGEMENT found in Eureka");
        }

        int index = counter.getAndIncrement() % instances.size();
        ServiceInstance instance = instances.get(index);

        String host = instance.getHost();
        String grpcPort = instance.getMetadata().get("grpc.port");

        log.info("Creating gRPC channel to User Service at {}:{} (instance {})",
                host, grpcPort, instance.getInstanceId());

        return ManagedChannelBuilder
                .forAddress(host, Integer.parseInt(grpcPort))
                .usePlaintext()
                .build();
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub(ManagedChannel userChannel) {
        return UserServiceGrpc.newBlockingStub(userChannel);
    }
}
