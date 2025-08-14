package com.mvcweb.htmlfetcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;

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
    void testIndex() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("MVCWeb API"))
                .andExpect(jsonPath("$.endpoints").exists())
                .andExpect(jsonPath("$.endpoints./api/fetch-html").exists());
    }

    @Test
    void testFetchHtml_Success() throws Exception {
        // Arrange
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("url", "https://example.com");
        successResponse.put("html", "<html><body>Test</body></html>");
        successResponse.put("statusCode", 200);
        successResponse.put("contentType", "text/html");
        successResponse.put("contentLength", 31);

        when(htmlFetchService.fetchHtml("example.com")).thenReturn(successResponse);

        Map<String, String> request = new HashMap<>();
        request.put("url", "example.com");

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.html").value("<html><body>Test</body></html>"))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void testFetchHtml_MissingUrl() throws Exception {
        Map<String, String> request = new HashMap<>();
        // No URL provided

        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter is required"));
    }

    @Test
    void testFetchHtml_EmptyUrl() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", "");

        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter cannot be blank"));
    }

    @Test
    void testFetchHtml_NullUrl() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", null);

        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter cannot be blank"));
    }

    @Test
    void testFetchHtml_ServiceException() throws Exception {
        when(htmlFetchService.fetchHtml(any())).thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("url", "https://example.com");

        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testFetchHtml_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest()); // Spring returns 400 for JSON parse errors
    }

    @Test
    void testFetchHtml_WrongContentType() throws Exception {
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("url=https://example.com"))
                .andExpect(status().isUnsupportedMediaType()); // Spring returns 415 for unsupported media type
    }

    @Test
    void testFetchHtml_NullRequest() throws Exception {
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        // Spring returns a default error response for missing request body, 
        // not our custom JSON response, so we can't check for specific JSON fields
    }

    @Test
    void testFetchHtml_WhitespaceUrl() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("url", "   ");

        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("URL parameter cannot be blank"));
    }

    @Test
    void testFetchHtml_SuccessWithTrimming() throws Exception {
        // Arrange
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("url", "https://example.com");
        successResponse.put("html", "<html><body>Test</body></html>");
        successResponse.put("statusCode", 200);
        successResponse.put("contentType", "text/html");
        successResponse.put("contentLength", 31);

        when(htmlFetchService.fetchHtml("example.com")).thenReturn(successResponse);

        Map<String, String> request = new HashMap<>();
        request.put("url", "  example.com  ");

        // Act & Assert
        mockMvc.perform(post("/api/fetch-html")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}