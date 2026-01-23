package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.proto.UserRequest;
import com.dpp.ddp_study_management.proto.UserResponse;
import com.dpp.ddp_study_management.proto.UserServiceGrpc;
import com.dpp.ddp_study_management.service.UserService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class UserGrpcController extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserGrpcController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUserById(
            UserRequest request,
            StreamObserver<UserResponse> responseObserver) {

        userService.getUserById(request.getId())
                .switchIfEmpty(Mono.error(
                        Status.NOT_FOUND
                                .withDescription("User not found")
                                .asRuntimeException()
                ))
                .map(this::mapToGrpc)
                .subscribe(
                        responseObserver::onNext,
                        responseObserver::onError,
                        responseObserver::onCompleted
                );

    }

    private com.dpp.ddp_study_management.proto.UserResponse mapToGrpc(
            com.dpp.ddp_study_management.dto.response.user.UserResponse user) {

        return com.dpp.ddp_study_management.proto.UserResponse.newBuilder()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setRole(user.getRole().name())
                .setCreatedAt(user.getCreatedAt().toString())
                .build();
    }

}

