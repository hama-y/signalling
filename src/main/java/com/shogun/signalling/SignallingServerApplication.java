package com.shogun.signalling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SignallingServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignallingServerApplication.class, args);
	}

}
