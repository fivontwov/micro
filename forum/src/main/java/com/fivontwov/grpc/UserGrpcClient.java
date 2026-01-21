package com.fivontwov.grpc;
import com.fivontwov.user.proto.UserRequest;
import com.fivontwov.user.proto.UserResponse;
import com.fivontwov.user.proto.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserGrpcClient {

    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    public Optional<UserResponse> getUserById(long id) {
        UserRequest request = UserRequest.newBuilder()
                .setId(id)
                .build();

        try {
            UserResponse response = userStub.getUserById(request);
            return Optional.of(response);

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.warn("User not found with id {}", id);
                return Optional.empty();
            }

            log.error("Error calling UserService.getUserById, id={}", id, e);
            throw e;
        }
    }
}
