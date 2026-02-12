//package com.dpp.ddp_study_management;
//
//import com.dpp.ddp_study_management.proto.UserRequest;
//import com.dpp.ddp_study_management.proto.UserResponse;
//import com.dpp.ddp_study_management.proto.UserServiceGrpc;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class UserGrpcTest {
//
//    private static ManagedChannel channel;
//    private static UserServiceGrpc.UserServiceBlockingStub stub;
//
//    @BeforeAll
//    static void setup() {
//        channel = ManagedChannelBuilder
//                .forAddress("localhost", 2)
//                .usePlaintext()
//                .build();
//
//        stub = UserServiceGrpc.newBlockingStub(channel);
//    }
//
//    @AfterAll
//    static void teardown() {
//        channel.shutdown();
//    }
//
//    @Test
//    void getUserById_shouldReturnUser() {
//        UserRequest request = UserRequest.newBuilder()
//                .setId(1L)
//                .build();
//
//        UserResponse response = stub.getUserById(request);
//
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//        assertEquals("ADMIN", response.getRole()); // ví dụ
//        System.out.println(response);
//    }
//}
