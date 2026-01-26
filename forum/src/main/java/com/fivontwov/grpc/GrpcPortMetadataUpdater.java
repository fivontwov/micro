//package com.fivontwov.grpc;
//
//import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
//import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
//import org.springframework.context.event.EventListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GrpcPortMetadataUpdater {
//
//    private final EurekaInstanceConfigBean eurekaInstanceConfigBean;
//
//    public GrpcPortMetadataUpdater(EurekaInstanceConfigBean eurekaInstanceConfigBean) {
//        this.eurekaInstanceConfigBean = eurekaInstanceConfigBean;
//    }
//
//    @EventListener
//    public void onGrpcServerStarted(GrpcServerStartedEvent event) {
//        int actualPort = event.getServer().getPort();
//        eurekaInstanceConfigBean.getMetadataMap()
//                .put("grpc.port", String.valueOf(actualPort));
//    }
//}
//
