package com.sarjom.citisci;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.sarjom.citisci")
public class CitisciApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitisciApplication.class, args);
	}

}
