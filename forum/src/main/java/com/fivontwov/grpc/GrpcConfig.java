package com.fivontwov.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fivontwov.user.proto.UserServiceGrpc;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class GrpcConfig {

    @Value("${grpc.user.host:localhost}")
    private String userServiceHost;

    @Value("${grpc.user.port:9090}")
    private int userServicePort;

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel userChannel() {
        log.info("Creating gRPC channel to User Service at {}:{}",
                userServiceHost, userServicePort);

        return ManagedChannelBuilder
                .forAddress(userServiceHost, userServicePort)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .idleTimeout(5, TimeUnit.MINUTES)
                .maxInboundMessageSize(4 * 1024 * 1024)
                .build();
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userBlockingStub(
            ManagedChannel userChannel) {

        log.info("Creating UserServiceBlockingStub");

        return UserServiceGrpc.newBlockingStub(userChannel);
    }
}
