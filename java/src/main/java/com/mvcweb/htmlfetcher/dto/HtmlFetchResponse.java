package com.mvcweb.htmlfetcher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Data Transfer Object for HTML fetch responses.
 * Contains the fetched HTML content and metadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HtmlFetchResponse {
    
    private boolean success;
    private String url;
    private String html;
    private Integer statusCode;
    private String contentType;
    private Integer contentLength;
    private String error;

    public HtmlFetchResponse() {}

    // Constructor for success response
    public HtmlFetchResponse(boolean success, String url, String html, 
                           Integer statusCode, String contentType, Integer contentLength) {
        this.success = success;
        this.url = url;
        this.html = html;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    // Constructor for error response
    public HtmlFetchResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Static factory methods
    public static HtmlFetchResponse success(String url, String html, 
                                          Integer statusCode, String contentType) {
        return new HtmlFetchResponse(true, url, html, statusCode, contentType, 
                                   html != null ? html.length() : 0);
    }

    public static HtmlFetchResponse error(String error) {
        return new HtmlFetchResponse(false, error);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "HtmlFetchResponse{" +
                "success=" + success +
                ", url='" + url + '\'' +
                ", statusCode=" + statusCode +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + contentLength +
                ", error='" + error + '\'' +
                '}';
    }
}