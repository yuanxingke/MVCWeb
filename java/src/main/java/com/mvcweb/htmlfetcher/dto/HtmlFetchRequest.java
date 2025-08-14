package com.mvcweb.htmlfetcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for HTML fetch requests.
 * Contains the URL to fetch HTML content from.
 */
public class HtmlFetchRequest {
    
    @NotNull(message = "URL parameter is required")
    @NotBlank(message = "URL parameter cannot be blank")
    private String url;

    public HtmlFetchRequest() {}

    public HtmlFetchRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "HtmlFetchRequest{" +
                "url='" + url + '\'' +
                '}';
    }
}