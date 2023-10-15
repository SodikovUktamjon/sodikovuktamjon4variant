package com.example;

import lombok.RequiredArgsConstructor;
import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class UserServiceApplication {


	private final MyTelegramBot bot;



	public static void main(String[] args) {
		OpenCV.loadLocally();
		SpringApplication.run(UserServiceApplication.class, args);

	}
	



}
