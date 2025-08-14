# MVCWeb

A Flask web application that provides an API endpoint to fetch HTML content from user-provided URLs.

## Features

- RESTful API endpoint to fetch HTML content from any URL
- Comprehensive error handling for various network issues
- URL validation and automatic protocol addition
- Browser-like headers to avoid blocking
- JSON response format with detailed metadata

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd MVCWeb
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Run the application:
```bash
python app.py
```

The server will start on `http://localhost:5000`

## API Usage

### Endpoint: `/api/fetch-html`

**Method:** POST  
**Content-Type:** application/json

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
  "status_code": 200,
  "content_type": "text/html; charset=utf-8",
  "content_length": 1234
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Error description"
}
```

### Example Usage

**Using curl:**
```bash
curl -X POST http://localhost:5000/api/fetch-html \
  -H "Content-Type: application/json" \
  -d '{"url": "https://httpbin.org/html"}'
```

**Using Python requests:**
```python
import requests

response = requests.post('http://localhost:5000/api/fetch-html', 
                        json={'url': 'https://httpbin.org/html'})
data = response.json()

if data['success']:
    print(f"HTML content length: {data['content_length']}")
    print(f"Content type: {data['content_type']}")
else:
    print(f"Error: {data['error']}")
```

## Error Handling

The API handles various types of errors:

- **400 Bad Request**: Missing or invalid URL parameter
- **408 Request Timeout**: Server took too long to respond (30s timeout)
- **503 Service Unavailable**: Connection error to target URL
- **HTTP Status Codes**: Returns the actual HTTP error from the target server
- **500 Internal Server Error**: Unexpected server errors

## API Documentation

Visit `http://localhost:5000/` to see the API documentation and available endpoints.

## Security Considerations

- The API includes a 30-second timeout to prevent hanging requests
- Uses browser-like headers to avoid being blocked by anti-bot measures
- Validates URL format before making requests
- Comprehensive error handling to prevent information leakage
