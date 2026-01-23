package com.fivontwov.grpc;
import com.fivontwov.user.proto.UserRequest;
import com.fivontwov.user.proto.UserResponse;
import com.fivontwov.user.proto.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGrpcClient {
    @GrpcClient("user-service")
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public Optional<UserResponse> getUserById(long id) {
        UserRequest request = UserRequest.newBuilder()
                .setId(id)
                .build();

        try {
            UserResponse response = userStub.getUserById(request);
            return Optional.of(response);

        } catch (StatusRuntimeException e) {
            log.error("Error calling UserService.getUserById, id={}", id, e);
            return Optional.empty();
        }
    }
}
