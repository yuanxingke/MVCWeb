package com.mvcweb.htmlfetcher.exception;

/**
 * Custom exception thrown when a provided URL is invalid.
 */
public class InvalidUrlException extends Exception {

    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}