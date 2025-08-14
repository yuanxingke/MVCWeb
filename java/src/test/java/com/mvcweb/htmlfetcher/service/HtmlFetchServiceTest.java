package com.mvcweb.htmlfetcher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HtmlFetchService.
 * Note: These are integration tests that make real HTTP calls.
 * In a production environment, you might want to mock the HTTP client.
 */
@ExtendWith(MockitoExtension.class)
class HtmlFetchServiceTest {

    private HtmlFetchService htmlFetchService;

    @BeforeEach
    void setUp() {
        htmlFetchService = new HtmlFetchService();
    }

    @Test
    void testFetchHtml_ValidUrl() {
        // This test makes a real HTTP call to httpbin.org
        String url = "httpbin.org/html";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("https://httpbin.org/html", result.get("url"));
        assertNotNull(result.get("html"));
        assertTrue(result.get("html").toString().contains("<html>"));
        assertEquals(200, result.get("statusCode"));
        assertNotNull(result.get("contentType"));
        assertTrue(((Integer) result.get("contentLength")) > 0);
    }

    @Test
    void testFetchHtml_UrlWithProtocol() {
        String url = "https://httpbin.org/html";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("https://httpbin.org/html", result.get("url"));
    }

    @Test
    void testFetchHtml_InvalidUrl() {
        String url = "not-a-valid-url";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("Invalid URL format"));
    }

    @Test
    void testFetchHtml_EmptyUrl() {
        String url = "";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
    }

    @Test
    void testFetchHtml_NullUrl() {
        String url = null;
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
    }

    @Test
    void testFetchHtml_UrlWithoutDomain() {
        String url = "just-text";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("Invalid URL format"));
    }

    @Test
    void testFetchHtml_NonExistentDomain() {
        String url = "https://this-domain-should-not-exist-12345.com";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        // Could be connection error or DNS resolution error
        assertTrue(result.get("error").toString().contains("error") || 
                  result.get("error").toString().contains("Connection"));
    }

    @Test
    void testFetchHtml_Localhost() {
        String url = "localhost:8080";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        // This will likely fail with connection error since no server is running
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
    }

    @Test
    void testFetchHtml_IpAddress() {
        String url = "127.0.0.1:8080";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        // This will likely fail with connection error since no server is running
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
    }

    @Test
    void testFetchHtml_HttpsUrl() {
        String url = "https://httpbin.org/status/200";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(200, result.get("statusCode"));
    }

    @Test
    void testFetchHtml_HttpUrl() {
        // Note: Many sites redirect HTTP to HTTPS
        String url = "http://httpbin.org/html";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        // Should succeed (might be redirected to HTTPS)
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testFetchHtml_UrlWithPath() {
        String url = "httpbin.org/json";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("https://httpbin.org/json", result.get("url"));
        // This endpoint returns JSON, not HTML
        assertNotNull(result.get("html"));
        assertTrue(result.get("html").toString().contains("{"));
    }

    @Test
    void testFetchHtml_UrlWithQuery() {
        String url = "httpbin.org/get?param=value";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("https://httpbin.org/get?param=value", result.get("url"));
    }

    @Test
    void testFetchHtml_404Error() {
        String url = "https://httpbin.org/status/404";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("404"));
    }

    @Test
    void testFetchHtml_500Error() {
        String url = "https://httpbin.org/status/500";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("500"));
    }

    @Test
    void testFetchHtml_UrlWithSpecialCharacters() {
        String url = "httpbin.org/anything/test%20with%20spaces";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("https://httpbin.org/anything/test%20with%20spaces", result.get("url"));
    }

    @Test
    void testFetchHtml_LongUrl() {
        String url = "httpbin.org/anything/" + "a".repeat(1000);
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testFetchHtml_UrlWithPort() {
        String url = "httpbin.org:80/html";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        // Port 80 with HTTPS might fail, so we check that it handles gracefully
        if ((Boolean) result.get("success")) {
            assertEquals("https://httpbin.org:80/html", result.get("url"));
        } else {
            assertNotNull(result.get("error"));
            // This is expected to fail due to SSL/port mismatch
        }
    }

    @Test
    void testFetchHtml_SubdomainUrl() {
        String url = "www.httpbin.org/html";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        // This might fail if subdomain doesn't exist, but should handle gracefully
        if ((Boolean) result.get("success")) {
            assertEquals("https://www.httpbin.org/html", result.get("url"));
        } else {
            assertNotNull(result.get("error"));
        }
    }

    @Test
    void testFetchHtml_UrlWithFragment() {
        String url = "httpbin.org/html#section";
        
        Map<String, Object> result = htmlFetchService.fetchHtml(url);
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        // Fragment should be preserved in URL
        assertEquals("https://httpbin.org/html#section", result.get("url"));
    }
}