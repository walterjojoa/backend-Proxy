package com.taller.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendProxyApplication.class, args);
	}

}
