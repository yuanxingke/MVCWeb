package com.mvcweb.htmlfetcher.controller;

import com.mvcweb.htmlfetcher.dto.HtmlFetchRequest;
import com.mvcweb.htmlfetcher.dto.HtmlFetchResponse;
import com.mvcweb.htmlfetcher.service.HtmlFetchService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for HTML fetching endpoints.
 * Provides API endpoints to fetch HTML content from user-provided URLs.
 */
@RestController
@RequestMapping("/api")
public class HtmlFetchController {

    private static final Logger logger = LoggerFactory.getLogger(HtmlFetchController.class);

    private final HtmlFetchService htmlFetchService;

    @Autowired
    public HtmlFetchController(HtmlFetchService htmlFetchService) {
        this.htmlFetchService = htmlFetchService;
    }

    /**
     * Root endpoint that provides API documentation.
     *
     * @return API documentation information
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "MVCWeb API");
        
        Map<String, Object> endpoints = new HashMap<>();
        Map<String, Object> fetchHtmlEndpoint = new HashMap<>();
        fetchHtmlEndpoint.put("method", "POST");
        fetchHtmlEndpoint.put("description", "Fetch HTML content from a provided URL");
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("url", "The URL to fetch HTML content from");
        fetchHtmlEndpoint.put("parameters", parameters);
        
        endpoints.put("/api/fetch-html", fetchHtmlEndpoint);
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to fetch HTML content from a provided URL.
     *
     * @param request The HTML fetch request containing the URL
     * @param bindingResult Validation results
     * @return ResponseEntity containing the HTML content or error information
     */
    @PostMapping("/fetch-html")
    public ResponseEntity<HtmlFetchResponse> fetchHtml(
            @Valid @RequestBody HtmlFetchRequest request,
            BindingResult bindingResult) {

        logger.info("Received HTML fetch request: {}", request);

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid request parameters");
            
            logger.error("Validation error: {}", errorMessage);
            return ResponseEntity.badRequest()
                    .body(HtmlFetchResponse.error(errorMessage));
        }

        // Fetch HTML content
        HtmlFetchResponse response = htmlFetchService.fetchHtml(request.getUrl());

        // Return appropriate HTTP status based on response
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Determine HTTP status code based on error type
            if (response.getError().contains("timeout")) {
                return ResponseEntity.status(408).body(response); // Request Timeout
            } else if (response.getError().contains("Connection error")) {
                return ResponseEntity.status(503).body(response); // Service Unavailable
            } else if (response.getError().contains("Invalid URL")) {
                return ResponseEntity.badRequest().body(response); // Bad Request
            } else {
                return ResponseEntity.status(500).body(response); // Internal Server Error
            }
        }
    }

    /**
     * Global exception handler for validation errors.
     *
     * @param e The exception
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HtmlFetchResponse> handleException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(500)
                .body(HtmlFetchResponse.error("Internal server error occurred while processing request"));
    }
}