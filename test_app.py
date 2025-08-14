import unittest
import json
from unittest.mock import patch, Mock
import requests
from app import app

class TestFlaskApp(unittest.TestCase):
    
    def setUp(self):
        """Set up test client and test data"""
        self.app = app.test_client()
        self.app.testing = True
        
        # Test URLs
        self.valid_url = "https://httpbin.org/html"
        self.invalid_url = "not-a-url"
        self.url_without_protocol = "example.com"
        
        # Mock HTML response
        self.mock_html = """<!DOCTYPE html>
<html>
<head><title>Test Page</title></head>
<body><h1>Hello World</h1></body>
</html>"""
    
    def test_index_endpoint(self):
        """Test the root endpoint returns API documentation"""
        response = self.app.get('/')
        self.assertEqual(response.status_code, 200)
        
        data = json.loads(response.data)
        self.assertIn('message', data)
        self.assertIn('endpoints', data)
        self.assertIn('/api/fetch-html', data['endpoints'])
        self.assertEqual(data['message'], 'MVCWeb API')
    
    def test_fetch_html_missing_json(self):
        """Test fetch_html endpoint with missing JSON data"""
        response = self.app.post('/api/fetch-html')
        self.assertEqual(response.status_code, 400)
        
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('Invalid JSON data or Content-Type must be application/json', data['error'])
    
    def test_fetch_html_missing_url_parameter(self):
        """Test fetch_html endpoint with missing URL parameter"""
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({}),
                                content_type='application/json')
        self.assertEqual(response.status_code, 400)
        
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('URL parameter is required', data['error'])
    
    def test_fetch_html_invalid_url_format(self):
        """Test fetch_html endpoint with invalid URL format"""
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.invalid_url}),
                                content_type='application/json')
        self.assertEqual(response.status_code, 400)
        
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('Invalid URL format', data['error'])
    
    def test_fetch_html_empty_url(self):
        """Test fetch_html endpoint with empty URL"""
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': ''}),
                                content_type='application/json')
        self.assertEqual(response.status_code, 400)
        
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('Invalid URL format', data['error'])
    
    def test_fetch_html_whitespace_url(self):
        """Test fetch_html endpoint with whitespace-only URL"""
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': '   '}),
                                content_type='application/json')
        self.assertEqual(response.status_code, 400)
        
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('Invalid URL format', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_successful_request(self, mock_get):
        """Test successful HTML fetching"""
        # Mock successful response
        mock_response = Mock()
        mock_response.text = self.mock_html
        mock_response.status_code = 200
        mock_response.headers = {'content-type': 'text/html; charset=utf-8'}
        mock_get.return_value = mock_response
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        
        self.assertTrue(data['success'])
        self.assertEqual(data['url'], self.valid_url)
        self.assertEqual(data['html'], self.mock_html)
        self.assertEqual(data['status_code'], 200)
        self.assertEqual(data['content_type'], 'text/html; charset=utf-8')
        self.assertEqual(data['content_length'], len(self.mock_html))
    
    @patch('app.requests.get')
    def test_fetch_html_url_without_protocol(self, mock_get):
        """Test URL without protocol gets https:// added"""
        mock_response = Mock()
        mock_response.text = self.mock_html
        mock_response.status_code = 200
        mock_response.headers = {'content-type': 'text/html; charset=utf-8'}
        mock_get.return_value = mock_response
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.url_without_protocol}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        
        self.assertTrue(data['success'])
        self.assertEqual(data['url'], f'https://{self.url_without_protocol}')
        
        # Verify the mock was called with the corrected URL
        mock_get.assert_called_once()
        args, kwargs = mock_get.call_args
        self.assertEqual(args[0], f'https://{self.url_without_protocol}')
    
    @patch('app.requests.get')
    def test_fetch_html_non_html_content(self, mock_get):
        """Test fetching non-HTML content"""
        mock_response = Mock()
        mock_response.text = '{"key": "value"}'
        mock_response.status_code = 200
        mock_response.headers = {'content-type': 'application/json'}
        mock_get.return_value = mock_response
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        
        self.assertTrue(data['success'])
        self.assertEqual(data['content_type'], 'application/json')
        self.assertEqual(data['html'], '{"key": "value"}')
    
    @patch('app.requests.get')
    def test_fetch_html_timeout_error(self, mock_get):
        """Test timeout error handling"""
        mock_get.side_effect = requests.exceptions.Timeout()
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 408)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('Request timeout', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_connection_error(self, mock_get):
        """Test connection error handling"""
        mock_get.side_effect = requests.exceptions.ConnectionError()
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 503)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('Connection error', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_http_error_404(self, mock_get):
        """Test HTTP 404 error handling"""
        mock_response = Mock()
        mock_response.status_code = 404
        mock_response.reason = 'Not Found'
        
        http_error = requests.exceptions.HTTPError()
        http_error.response = mock_response
        mock_get.side_effect = http_error
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 404)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('HTTP error: 404 - Not Found', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_http_error_500(self, mock_get):
        """Test HTTP 500 error handling"""
        mock_response = Mock()
        mock_response.status_code = 500
        mock_response.reason = 'Internal Server Error'
        
        http_error = requests.exceptions.HTTPError()
        http_error.response = mock_response
        mock_get.side_effect = http_error
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 500)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('HTTP error: 500 - Internal Server Error', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_request_exception(self, mock_get):
        """Test general request exception handling"""
        mock_get.side_effect = requests.exceptions.RequestException("Network error")
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 500)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('Request error: Network error', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_unexpected_exception(self, mock_get):
        """Test unexpected exception handling"""
        mock_get.side_effect = Exception("Unexpected error")
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 500)
        data = json.loads(response.data)
        
        self.assertFalse(data['success'])
        self.assertIn('Internal server error occurred', data['error'])
    
    @patch('app.requests.get')
    def test_fetch_html_request_headers(self, mock_get):
        """Test that proper headers are sent with the request"""
        mock_response = Mock()
        mock_response.text = self.mock_html
        mock_response.status_code = 200
        mock_response.headers = {'content-type': 'text/html'}
        mock_get.return_value = mock_response
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': self.valid_url}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        
        # Verify the request was made with proper headers
        mock_get.assert_called_once()
        args, kwargs = mock_get.call_args
        
        self.assertIn('headers', kwargs)
        headers = kwargs['headers']
        self.assertIn('User-Agent', headers)
        self.assertIn('Mozilla', headers['User-Agent'])
        self.assertIn('Accept', headers)
        self.assertIn('text/html', headers['Accept'])
        
        # Verify timeout and redirects
        self.assertEqual(kwargs['timeout'], 30)
        self.assertTrue(kwargs['allow_redirects'])
    
    @patch('app.requests.get')
    def test_fetch_html_url_with_trailing_spaces(self, mock_get):
        """Test URL with trailing spaces gets trimmed"""
        mock_response = Mock()
        mock_response.text = self.mock_html
        mock_response.status_code = 200
        mock_response.headers = {'content-type': 'text/html'}
        mock_get.return_value = mock_response
        
        url_with_spaces = "  " + self.valid_url + "  "
        
        response = self.app.post('/api/fetch-html',
                                data=json.dumps({'url': url_with_spaces}),
                                content_type='application/json')
        
        self.assertEqual(response.status_code, 200)
        data = json.loads(response.data)
        
        self.assertTrue(data['success'])
        self.assertEqual(data['url'], self.valid_url)
        
        # Verify the mock was called with the trimmed URL
        mock_get.assert_called_once()
        args, kwargs = mock_get.call_args
        self.assertEqual(args[0], self.valid_url)
    
    def test_fetch_html_wrong_http_method(self):
        """Test that GET method is not allowed on fetch-html endpoint"""
        response = self.app.get('/api/fetch-html')
        self.assertEqual(response.status_code, 405)  # Method Not Allowed
    
    def test_fetch_html_wrong_content_type(self):
        """Test fetch_html endpoint with wrong content type"""
        response = self.app.post('/api/fetch-html',
                                data='url=https://example.com',
                                content_type='application/x-www-form-urlencoded')
        
        # Should return 400 because JSON parsing will fail
        self.assertEqual(response.status_code, 400)
        data = json.loads(response.data)
        self.assertFalse(data['success'])
        self.assertIn('Invalid JSON data or Content-Type must be application/json', data['error'])

class TestURLValidation(unittest.TestCase):
    """Additional tests focused on URL validation edge cases"""
    
    def setUp(self):
        self.app = app.test_client()
        self.app.testing = True
    
    def test_various_invalid_urls(self):
        """Test various invalid URL formats"""
        invalid_urls = [
            "javascript:alert('xss')",
            "ftp://example.com",
            "file:///etc/passwd",
            "http://",
            "https://",
            "://example.com",
            "http:example.com",
            "example",
            "192.168.1.1",  # IP without protocol
        ]
        
        for invalid_url in invalid_urls:
            with self.subTest(url=invalid_url):
                response = self.app.post('/api/fetch-html',
                                        data=json.dumps({'url': invalid_url}),
                                        content_type='application/json')
                
                data = json.loads(response.data)
                self.assertFalse(data['success'], f"URL {invalid_url} should be invalid")
    
    def test_valid_urls_formats(self):
        """Test that various valid URL formats pass validation"""
        valid_urls = [
            "https://example.com",
            "http://example.com",
            "https://www.example.com",
            "https://subdomain.example.com",
            "https://example.com/path",
            "https://example.com:8080",
            "https://example.com/path?query=value",
            "https://example.com/path#fragment",
            "example.com",  # Should get https:// added
            "www.example.com",  # Should get https:// added
        ]
        
        with patch('app.requests.get') as mock_get:
            mock_response = Mock()
            mock_response.text = "<html></html>"
            mock_response.status_code = 200
            mock_response.headers = {'content-type': 'text/html'}
            mock_get.return_value = mock_response
            
            for valid_url in valid_urls:
                with self.subTest(url=valid_url):
                    response = self.app.post('/api/fetch-html',
                                            data=json.dumps({'url': valid_url}),
                                            content_type='application/json')
                    
                    self.assertEqual(response.status_code, 200, 
                                   f"URL {valid_url} should be valid")
                    data = json.loads(response.data)
                    self.assertTrue(data['success'], 
                                  f"URL {valid_url} should be valid")

if __name__ == '__main__':
    unittest.main()