package com.sun.wineshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WineShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(WineShopApplication.class, args);
	}

}
