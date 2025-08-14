package com.mvcweb.htmlfetcher.controller;

import com.mvcweb.htmlfetcher.service.HtmlFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private HtmlFetchService htmlFetchService;

    /**
     * Root endpoint providing API documentation
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
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
     * Fetch HTML content from a user-provided URL
     */
    @PostMapping("/fetch-html")
    public ResponseEntity<Map<String, Object>> fetchHtml(@RequestBody Map<String, String> request) {
        logger.info("Received HTML fetch request: {}", request);

        // Validate request
        if (request == null || !request.containsKey("url")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "URL parameter is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String url = request.get("url");
        if (url == null || url.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "URL parameter cannot be blank");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Map<String, Object> result = htmlFetchService.fetchHtml(url.trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error fetching HTML: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}