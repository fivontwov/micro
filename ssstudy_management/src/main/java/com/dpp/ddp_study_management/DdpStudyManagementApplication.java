package com.dpp.ddp_study_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableCaching
@EnableDiscoveryClient
public class DdpStudyManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DdpStudyManagementApplication.class, args);
	}

}