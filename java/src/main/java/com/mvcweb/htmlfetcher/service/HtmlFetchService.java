package com.mvcweb.htmlfetcher.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for fetching HTML content from URLs.
 * Handles URL validation, HTTP requests, and error management.
 */
@Service
public class HtmlFetchService {

    private static final Logger logger = LoggerFactory.getLogger(HtmlFetchService.class);
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * Fetch HTML content from the provided URL.
     *
     * @param url The URL to fetch HTML from
     * @return Map containing the result or error information
     */
    public Map<String, Object> fetchHtml(String url) {
        logger.info("Fetching HTML from URL: {}", url);

        try {
            // Add protocol if missing
            String processedUrl = addProtocolIfMissing(url);
            
            // Validate URL
            validateUrl(processedUrl);

            // Fetch HTML content
            return performHttpRequest(processedUrl);

        } catch (Exception e) {
            logger.error("Error fetching HTML from URL {}: {}", url, e.getMessage());
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * Add https:// protocol if missing from URL
     */
    private String addProtocolIfMissing(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

    /**
     * Validate URL format
     */
    private void validateUrl(String url) throws Exception {
        try {
            URL urlObj = new URL(url);
            
            // Check if host is valid
            String host = urlObj.getHost();
            if (host == null || host.trim().isEmpty()) {
                throw new Exception("Invalid URL format. Please provide a complete URL with protocol (http/https)");
            }
            
            // Additional validation for host to ensure it looks like a domain
            if (!host.contains(".") && !host.equals("localhost") && !host.equals("127.0.0.1")) {
                // Check if it's an IP address
                if (!isValidIpAddress(host)) {
                    throw new Exception("Invalid URL format. Please provide a complete URL with protocol (http/https)");
                }
            }
            
        } catch (MalformedURLException e) {
            throw new Exception("Invalid URL format. Please provide a complete URL with protocol (http/https)");
        }
    }

    /**
     * Check if string is a valid IP address
     */
    private boolean isValidIpAddress(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Perform the actual HTTP request
     */
    private Map<String, Object> performHttpRequest(String url) throws Exception {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(TIMEOUT_SECONDS))
                .setResponseTimeout(Timeout.ofSeconds(TIMEOUT_SECONDS))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {

            HttpGet httpGet = new HttpGet(URI.create(url));
            
            // Set browser-like headers
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Language", "en-US,en;q=0.5");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
            httpGet.setHeader("Connection", "keep-alive");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getCode();
                
                if (statusCode >= 400) {
                    throw new Exception("HTTP error: " + statusCode + " - " + response.getReasonPhrase());
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String htmlContent = EntityUtils.toString(entity);
                    String contentType = response.getFirstHeader("Content-Type") != null ? 
                                       response.getFirstHeader("Content-Type").getValue() : "";
                    
                    // Check if content is HTML
                    if (!contentType.toLowerCase().contains("text/html")) {
                        logger.warn("Content type is not HTML: {}", contentType);
                    }

                    return createSuccessResponse(url, htmlContent, statusCode, contentType);
                } else {
                    throw new Exception("No content received from URL");
                }
            }
        } catch (IOException e) {
            if (e.getMessage().contains("timeout")) {
                throw new Exception("Request timeout. The server took too long to respond.");
            } else if (e.getMessage().contains("Connection") || e.getMessage().contains("connection")) {
                throw new Exception("Connection error. Unable to connect to the provided URL.");
            } else {
                throw new Exception("Request error: " + e.getMessage());
            }
        }
    }

    /**
     * Create success response map
     */
    private Map<String, Object> createSuccessResponse(String url, String html, int statusCode, String contentType) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("url", url);
        response.put("html", html);
        response.put("statusCode", statusCode);
        response.put("contentType", contentType);
        response.put("contentLength", html.length());
        return response;
    }

    /**
     * Create error response map
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorMessage);
        return response;
    }
}