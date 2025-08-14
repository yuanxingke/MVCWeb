package com.mvcweb.htmlfetcher.service;

import com.mvcweb.htmlfetcher.dto.HtmlFetchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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
        // In production, you might want to mock this
        String url = "https://httpbin.org/html";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals(url, response.getUrl());
        assertNotNull(response.getHtml());
        assertNotNull(response.getStatusCode());
        assertNotNull(response.getContentType());
        assertTrue(response.getContentLength() > 0);
        assertNull(response.getError());
    }

    @Test
    void testFetchHtml_NullUrl() {
        HtmlFetchResponse response = htmlFetchService.fetchHtml(null);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertEquals("URL parameter is required", response.getError());
    }

    @Test
    void testFetchHtml_EmptyUrl() {
        HtmlFetchResponse response = htmlFetchService.fetchHtml("");
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertEquals("URL parameter is required", response.getError());
    }

    @Test
    void testFetchHtml_WhitespaceUrl() {
        HtmlFetchResponse response = htmlFetchService.fetchHtml("   ");
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertEquals("URL parameter is required", response.getError());
    }

    @Test
    void testFetchHtml_InvalidUrlFormat() {
        HtmlFetchResponse response = htmlFetchService.fetchHtml("not-a-url");
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Invalid URL format"));
    }

    @Test
    void testFetchHtml_UrlWithoutProtocol() {
        // This test makes a real HTTP call
        String url = "httpbin.org/html";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals("https://httpbin.org/html", response.getUrl());
        assertNotNull(response.getHtml());
    }

    @Test
    void testFetchHtml_UrlWithTrailingSpaces() {
        // This test makes a real HTTP call
        String url = "  https://httpbin.org/html  ";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals("https://httpbin.org/html", response.getUrl());
        assertNotNull(response.getHtml());
    }

    @Test
    void testFetchHtml_NonExistentDomain() {
        String url = "https://this-domain-should-not-exist-12345.com";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Connection error") || 
                  response.getError().contains("timeout"));
    }

    @Test
    void testFetchHtml_InvalidProtocol() {
        String url = "ftp://example.com";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        // This should be handled by URL validation or HTTP client
        // The exact behavior depends on implementation
        assertNotNull(response);
        assertFalse(response.isSuccess());
    }

    @Test
    void testFetchHtml_MalformedUrl() {
        String url = "https://";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Invalid URL format"));
    }

    @Test
    void testFetchHtml_JavascriptUrl() {
        String url = "javascript:alert('xss')";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        // Should be rejected due to invalid protocol or format
    }

    @Test
    void testFetchHtml_FileUrl() {
        String url = "file:///etc/passwd";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertFalse(response.isSuccess());
        assertNotNull(response.getError());
        // Should be rejected due to invalid protocol
    }

    @Test
    void testFetchHtml_LocalhostUrl() {
        String url = "http://localhost:8080";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        // This might succeed or fail depending on whether something is running on localhost:8080
        // The important thing is that it doesn't crash and returns a valid response
        assertNotNull(response);
        if (!response.isSuccess()) {
            assertNotNull(response.getError());
        }
    }

    @Test
    void testFetchHtml_IpAddress() {
        String url = "http://127.0.0.1:8080";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        // Similar to localhost test
        assertNotNull(response);
        if (!response.isSuccess()) {
            assertNotNull(response.getError());
        }
    }

    @Test
    void testFetchHtml_HttpsUrl() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org/json";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals(url, response.getUrl());
        assertNotNull(response.getHtml());
        // Content type should be application/json, not text/html
        assertTrue(response.getContentType().contains("json"));
    }

    @Test
    void testFetchHtml_HttpUrl() {
        // This test makes a real HTTP call
        String url = "http://httpbin.org/html";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals(url, response.getUrl());
        assertNotNull(response.getHtml());
    }

    @Test
    void testFetchHtml_UrlWithPort() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org:443/html";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getHtml());
    }

    @Test
    void testFetchHtml_UrlWithPath() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org/status/200";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    void testFetchHtml_UrlWithQuery() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org/get?test=value";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getHtml());
    }

    @Test
    void testFetchHtml_404Error() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org/status/404";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess()); // HTTP call succeeds, but returns 404
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testFetchHtml_500Error() {
        // This test makes a real HTTP call
        String url = "https://httpbin.org/status/500";
        
        HtmlFetchResponse response = htmlFetchService.fetchHtml(url);
        
        assertTrue(response.isSuccess()); // HTTP call succeeds, but returns 500
        assertEquals(500, response.getStatusCode());
    }
}