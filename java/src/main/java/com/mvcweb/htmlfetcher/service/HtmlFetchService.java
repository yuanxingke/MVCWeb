package com.mvcweb.htmlfetcher.service;

import com.mvcweb.htmlfetcher.dto.HtmlFetchResponse;
import com.mvcweb.htmlfetcher.exception.InvalidUrlException;
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
import java.util.concurrent.TimeUnit;

/**
 * Service class for fetching HTML content from URLs.
 * Handles URL validation, HTTP requests, and error handling.
 */
@Service
public class HtmlFetchService {

    private static final Logger logger = LoggerFactory.getLogger(HtmlFetchService.class);
    
    private static final int TIMEOUT_SECONDS = 30;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    /**
     * Fetches HTML content from the specified URL.
     *
     * @param url The URL to fetch HTML content from
     * @return HtmlFetchResponse containing the HTML content or error information
     */
    public HtmlFetchResponse fetchHtml(String url) {
        try {
            // Validate and normalize URL
            String normalizedUrl = validateAndNormalizeUrl(url);
            logger.info("Fetching HTML from: {}", normalizedUrl);

            // Create HTTP client with timeout configuration
            RequestConfig config = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.of(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .setConnectTimeout(Timeout.of(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .setResponseTimeout(Timeout.of(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .build();

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .build()) {

                // Create HTTP GET request
                HttpGet httpGet = new HttpGet(normalizedUrl);
                httpGet.setHeader("User-Agent", USER_AGENT);
                httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Language", "en-US,en;q=0.5");
                httpGet.setHeader("Accept-Encoding", "gzip, deflate");
                httpGet.setHeader("Connection", "keep-alive");

                // Execute request
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int statusCode = response.getCode();
                    String contentType = response.getFirstHeader("Content-Type") != null ? 
                            response.getFirstHeader("Content-Type").getValue() : "";

                    // Check if content is HTML
                    if (!contentType.toLowerCase().contains("text/html")) {
                        logger.warn("Content type is not HTML: {}", contentType);
                    }

                    // Get response body
                    HttpEntity entity = response.getEntity();
                    String html = entity != null ? EntityUtils.toString(entity, "UTF-8") : "";

                    return HtmlFetchResponse.success(normalizedUrl, html, statusCode, contentType);
                }
            }

        } catch (InvalidUrlException e) {
            logger.error("Invalid URL: {}", e.getMessage());
            return HtmlFetchResponse.error(e.getMessage());
        } catch (java.net.SocketTimeoutException | java.net.ConnectException e) {
            logger.error("Connection timeout or error for URL {}: {}", url, e.getMessage());
            return HtmlFetchResponse.error("Request timeout. The server took too long to respond.");
        } catch (IOException e) {
            logger.error("IO error while fetching URL {}: {}", url, e.getMessage());
            return HtmlFetchResponse.error("Connection error. Unable to connect to the provided URL.");
        } catch (Exception e) {
            logger.error("Unexpected error while fetching URL {}: {}", url, e.getMessage(), e);
            return HtmlFetchResponse.error("Internal server error occurred while fetching HTML");
        }
    }

    /**
     * Validates and normalizes the provided URL.
     *
     * @param url The URL to validate and normalize
     * @return The normalized URL
     * @throws InvalidUrlException if the URL is invalid
     */
    private String validateAndNormalizeUrl(String url) throws InvalidUrlException {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL parameter is required");
        }

        String trimmedUrl = url.trim();

        // Add protocol if missing
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            trimmedUrl = "https://" + trimmedUrl;
        }

        try {
            // Validate URL format
            URL parsedUrl = new URL(trimmedUrl);
            URI uri = parsedUrl.toURI();

            // Additional validation for domain format
            String host = parsedUrl.getHost();
            if (host == null || host.trim().isEmpty()) {
                throw new InvalidUrlException("Invalid URL format. Please provide a complete URL with protocol (http/https)");
            }

            // Check if host looks like a valid domain (contains dot) or is localhost/IP
            if (!host.equals("localhost") && !host.equals("127.0.0.1") && 
                !host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") && !host.contains(".")) {
                throw new InvalidUrlException("Invalid URL format. Please provide a complete URL with protocol (http/https)");
            }

            return uri.toString();

        } catch (MalformedURLException | URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format. Please provide a complete URL with protocol (http/https)");
        }
    }
}