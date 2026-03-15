/*
 * FILE: DemoApplication.java
 * PURPOSE: Main Spring Boot application entry point. Starts the embedded server,
 *          initializes the Spring context, loads configuration, and triggers any
 *          startup beans such as DataInitializer.
 *
 * METHODS:
 *  - main(args)
 *      Standard Java entry point. Delegates to SpringApplication.run(...) to boot
 *      the Employee Attendance System.
 *
 * HOW TO MODIFY:
 *  - To add application-wide startup customization: configure SpringApplication
 *    here before calling run(), or register ApplicationRunner/CommandLineRunner beans.
 *  - To change scan behavior: keep this class at the root package or configure
 *    @SpringBootApplication(scanBasePackages = ...).
 */
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
