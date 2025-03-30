package com.ednevnik.dnevnik;

import org.springframework.boot.SpringApplication;

public class TestDnevnikApplication {

	public static void main(String[] args) {
		SpringApplication.from(DnevnikApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
