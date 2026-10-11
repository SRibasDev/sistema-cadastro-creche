package com.particaolar.mundo.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SystemApplication {

	public static void main(String[] args) {
		// Fuso horário consistente em toda a JVM (complementa Jackson e JDBC).
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(SystemApplication.class, args);
	}

}
