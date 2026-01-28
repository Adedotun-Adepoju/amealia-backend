package com.backend.amealia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AmealiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmealiaApplication.class, args);
	}

}
