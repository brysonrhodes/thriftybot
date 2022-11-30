package com.brysonrhodes.api.smshandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.firestore.repository.config.EnableReactiveFirestoreRepositories;

@SpringBootApplication
@EnableReactiveFirestoreRepositories
public class SmsHandlerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmsHandlerApplication.class, args);
	}

}
