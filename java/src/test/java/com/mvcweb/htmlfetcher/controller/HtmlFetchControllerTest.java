package com.mvcweb.htmlfetcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvcweb.htmlfetcher.dto.HtmlFetchRequest;
import com.mvcweb.htmlfetcher.dto.HtmlFetchResponse;
import com.mvcweb.htmlfetcher.service.HtmlFetchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HtmlFetchController.
 */
@ExtendWith(MockitoExtension.class)
class HtmlFetchControllerTest {

    @Mock
    private HtmlFetchService htmlFetchService;

    @InjectMocks
    private HtmlFetchController htmlFetchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(htmlFetchController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetApiInfo() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("MVCWeb API"))
                .andExpect(jsonPath("$.endpoints").exists())
                .andExpect(jsonPath("$.endpoints['/api/fetch-html']").exists())
                .andExpect(jsonPath("$.endpoints['/api/fetch-html'].method").value("POST"))
                .andExpect(jsonPath("$.endpoints['/api/fetch-html'].description").value("Fetch HTML content from a provided URL"));
    }

    @Test
    void testFetchHtml_Success() throws Exception {
        // Arrange
        String testUrl = "https://httpbin.org/html";
        String testHtml = "<html><body><h1>Test</h1></body></html>";
        HtmlFetchRequest request = new HtmlFetchRequest(testUrl);
        HtmlFetchResponse mockResponse = HtmlFetchResponse.success(testUrl, testHtml, 200, "text/html");

        when(htmlFetchService.fetchHtml(testUrl)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value(testUrl))
                .andExpect(jsonPath("$.html").value(testHtml))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.contentType").value("text/html"))
                .andExpect(jsonPath("$.contentLength").value(testHtml.length()));
    }

    @Test
    void testFetchHtml_MissingUrl() throws Exception {
        // Arrange
        HtmlFetchRequest request = new HtmlFetchRequest();

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter is required"));
    }

    @Test
    void testFetchHtml_EmptyUrl() throws Exception {
        // Arrange
        HtmlFetchRequest request = new HtmlFetchRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter cannot be blank"));
    }

    @Test
    void testFetchHtml_InvalidUrl() throws Exception {
        // Arrange
        String invalidUrl = "not-a-url";
        HtmlFetchRequest request = new HtmlFetchRequest(invalidUrl);
        HtmlFetchResponse mockResponse = HtmlFetchResponse.error("Invalid URL format. Please provide a complete URL with protocol (http/https)");

        when(htmlFetchService.fetchHtml(invalidUrl)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid URL format. Please provide a complete URL with protocol (http/https)"));
    }

    @Test
    void testFetchHtml_TimeoutError() throws Exception {
        // Arrange
        String testUrl = "https://httpbin.org/html";
        HtmlFetchRequest request = new HtmlFetchRequest(testUrl);
        HtmlFetchResponse mockResponse = HtmlFetchResponse.error("Request timeout. The server took too long to respond.");

        when(htmlFetchService.fetchHtml(testUrl)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isRequestTimeout())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Request timeout. The server took too long to respond."));
    }

    @Test
    void testFetchHtml_ConnectionError() throws Exception {
        // Arrange
        String testUrl = "https://httpbin.org/html";
        HtmlFetchRequest request = new HtmlFetchRequest(testUrl);
        HtmlFetchResponse mockResponse = HtmlFetchResponse.error("Connection error. Unable to connect to the provided URL.");

        when(htmlFetchService.fetchHtml(testUrl)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Connection error. Unable to connect to the provided URL."));
    }

    @Test
    void testFetchHtml_InternalServerError() throws Exception {
        // Arrange
        String testUrl = "https://httpbin.org/html";
        HtmlFetchRequest request = new HtmlFetchRequest(testUrl);
        HtmlFetchResponse mockResponse = HtmlFetchResponse.error("Internal server error occurred while fetching HTML");

        when(htmlFetchService.fetchHtml(testUrl)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Internal server error occurred while fetching HTML"));
    }

    @Test
    void testFetchHtml_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isInternalServerError()); // Spring returns 500 for JSON parse errors
    }

    @Test
    void testFetchHtml_WrongContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("url=https://example.com"))
                .andExpect(status().isInternalServerError()); // Spring returns 500 for unsupported media type
    }
}