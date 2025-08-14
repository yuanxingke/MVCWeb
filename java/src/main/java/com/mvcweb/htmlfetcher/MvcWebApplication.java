package com.mvcweb.htmlfetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for MVCWeb HTML Fetcher.
 * 
 * This application provides REST API endpoints to fetch HTML content
 * from user-provided URLs with comprehensive error handling and validation.
 */
@SpringBootApplication
public class MvcWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcWebApplication.class, args);
    }
}