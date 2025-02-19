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
		System.setProperty("file.encoding", "UTF-8");
	}


	public static interface InlineKeyboardConstants {
		String EXCEL_FILE_DATA = "fileType/excel";
		String PDF_FILE_DATA = "fileType/pdf";
		String PDF = "\uD83D\uDCC3PDF";
		String EXCEL = "\uD83E\uDDFEEXCEL";
	}
}
