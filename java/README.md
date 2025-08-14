# MVCWeb HTML Fetcher (Java Implementation)

A Spring Boot application that provides REST API endpoints to fetch HTML content from user-provided URLs with comprehensive error handling and validation.

## Features

- RESTful API endpoint to fetch HTML content from any URL
- Comprehensive error handling for various network issues
- URL validation and automatic protocol addition
- Browser-like headers to avoid blocking
- JSON response format with detailed metadata
- Built with Spring Boot 3.2.0 and Java 17
- Comprehensive unit tests with JUnit 5
- Maven build system with code coverage reporting

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **HTTP Client**: Apache HttpClient 5
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven 3.9+
- **Code Coverage**: JaCoCo

## Project Structure

```
java/
├── src/
│   ├── main/
│   │   ├── java/com/mvcweb/htmlfetcher/
│   │   │   ├── MvcWebApplication.java          # Main Spring Boot application
│   │   │   ├── controller/
│   │   │   │   └── HtmlFetchController.java    # REST controller
│   │   │   ├── service/
│   │   │   │   └── HtmlFetchService.java       # Business logic service
│   │   │   ├── dto/
│   │   │   │   ├── HtmlFetchRequest.java       # Request DTO
│   │   │   │   └── HtmlFetchResponse.java      # Response DTO
│   │   │   └── exception/
│   │   │       └── InvalidUrlException.java    # Custom exception
│   │   └── resources/
│   │       └── application.properties          # Application configuration
│   └── test/
│       └── java/com/mvcweb/htmlfetcher/
│           ├── controller/
│           │   └── HtmlFetchControllerTest.java # Controller tests
│           └── service/
│               └── HtmlFetchServiceTest.java    # Service tests
├── pom.xml                                     # Maven configuration
└── README.md                                   # This file
```

## Prerequisites

- Java 17 or higher
- Maven 3.9 or higher

## Installation & Setup

1. **Clone the repository:**
```bash
git clone <repository-url>
cd MVCWeb/java
```

2. **Build the project:**
```bash
mvn clean compile
```

3. **Run tests:**
```bash
mvn test
```

4. **Run the application:**
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

## API Usage

### Base URL
```
http://localhost:8080
```

### Endpoint: `POST /api/fetch-html`

**Content-Type:** `application/json`

**Request Body:**
```json
{
  "url": "https://example.com"
}
```

**Success Response:**
```json
{
  "success": true,
  "url": "https://example.com",
  "html": "<!DOCTYPE html>...",
  "statusCode": 200,
  "contentType": "text/html; charset=utf-8",
  "contentLength": 1234
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Error description"
}
```

### API Documentation Endpoint: `GET /api/`

Returns information about available endpoints and their usage.

## Example Usage

### Using curl:
```bash
curl -X POST http://localhost:8080/api/fetch-html \
  -H "Content-Type: application/json" \
  -d '{"url": "https://httpbin.org/html"}'
```

### Using HTTPie:
```bash
http POST localhost:8080/api/fetch-html url=https://httpbin.org/html
```

### Using Java (RestTemplate):
```java
RestTemplate restTemplate = new RestTemplate();
HtmlFetchRequest request = new HtmlFetchRequest("https://httpbin.org/html");

ResponseEntity<HtmlFetchResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/fetch-html", 
    request, 
    HtmlFetchResponse.class
);

HtmlFetchResponse result = response.getBody();
if (result.isSuccess()) {
    System.out.println("HTML content length: " + result.getContentLength());
    System.out.println("Content type: " + result.getContentType());
} else {
    System.out.println("Error: " + result.getError());
}
```

## Error Handling

The API handles various types of errors with appropriate HTTP status codes:

- **400 Bad Request**: Missing or invalid URL parameter, validation errors
- **408 Request Timeout**: Server took too long to respond (30s timeout)
- **415 Unsupported Media Type**: Wrong Content-Type header
- **503 Service Unavailable**: Connection error to target URL
- **500 Internal Server Error**: Unexpected server errors

## Testing

### Running Tests

**Run all tests:**
```bash
mvn test
```

**Run tests with coverage:**
```bash
mvn test jacoco:report
```

**Run specific test class:**
```bash
mvn test -Dtest=HtmlFetchControllerTest
```

### Test Coverage

The project includes comprehensive unit tests:

- **Controller Tests**: Mock-based tests for REST endpoints
- **Service Tests**: Integration tests with real HTTP calls
- **Coverage Target**: 90%+ line coverage
- **Test Framework**: JUnit 5 with Mockito

### Test Structure

- **HtmlFetchControllerTest**: Tests for REST controller layer
  - API endpoint functionality
  - Request/response validation
  - Error handling scenarios
  - HTTP status code validation

- **HtmlFetchServiceTest**: Tests for service layer
  - URL validation and normalization
  - HTTP client integration
  - Error handling for various network conditions
  - Edge cases and security scenarios

## Building & Packaging

### Create JAR file:
```bash
mvn clean package
```

### Run the JAR:
```bash
java -jar target/html-fetcher-1.0.0.jar
```

### Create Docker image (optional):
```bash
# Add Dockerfile and build
docker build -t mvcweb-html-fetcher .
docker run -p 8080:8080 mvcweb-html-fetcher
```

## Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server port
server.port=8080

# Logging levels
logging.level.com.mvcweb.htmlfetcher=INFO

# JSON formatting
spring.jackson.serialization.indent-output=true

# Connection timeouts
server.tomcat.connection-timeout=30000
```

### Environment Variables

You can override configuration using environment variables:

```bash
export SERVER_PORT=9090
export LOGGING_LEVEL_COM_MVCWEB_HTMLFETCHER=DEBUG
java -jar target/html-fetcher-1.0.0.jar
```

## Security Considerations

- **Timeout Protection**: 30-second timeout prevents hanging requests
- **URL Validation**: Strict validation prevents malicious URLs
- **Protocol Filtering**: Only HTTP/HTTPS protocols allowed
- **Error Handling**: Comprehensive error handling prevents information leakage
- **Input Validation**: Bean validation for request parameters
- **Browser Headers**: Uses realistic headers to avoid bot detection

## Performance

- **Connection Pooling**: HTTP client uses connection pooling
- **Timeout Configuration**: Configurable timeouts for reliability
- **Resource Management**: Proper resource cleanup with try-with-resources
- **Async Support**: Built on Spring Boot's async capabilities

## Development

### IDE Setup

**IntelliJ IDEA:**
1. Import as Maven project
2. Set Project SDK to Java 17
3. Enable annotation processing
4. Install Spring Boot plugin

**VS Code:**
1. Install Java Extension Pack
2. Install Spring Boot Extension Pack
3. Open project folder

### Code Style

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments
- Maintain high test coverage (>90%)

## Troubleshooting

### Common Issues

**Port already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**Java version issues:**
```bash
# Check Java version
java -version
# Should be 17 or higher
```

**Maven build failures:**
```bash
# Clean and rebuild
mvn clean compile
```

**Test failures:**
```bash
# Run tests in verbose mode
mvn test -X
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.