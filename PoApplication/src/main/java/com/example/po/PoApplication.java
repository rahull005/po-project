package com.example.po;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PoApplication.class, args);
	}

}
