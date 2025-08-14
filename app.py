from flask import Flask, request, jsonify
import requests
from urllib.parse import urlparse
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)

@app.route('/')
def index():
    return jsonify({
        "message": "MVCWeb API",
        "endpoints": {
            "/api/fetch-html": {
                "method": "POST",
                "description": "Fetch HTML content from a provided URL",
                "parameters": {
                    "url": "The URL to fetch HTML content from"
                }
            }
        }
    })

@app.route('/api/fetch-html', methods=['POST'])
def fetch_html():
    """
    API endpoint to fetch HTML content from a user-provided URL
    """
    try:
        # Get URL from request
        data = request.get_json()
        if not data or 'url' not in data:
            return jsonify({
                'error': 'URL parameter is required',
                'success': False
            }), 400
        
        url = data['url'].strip()
        
        # Validate URL format
        parsed_url = urlparse(url)
        if not parsed_url.scheme or not parsed_url.netloc:
            return jsonify({
                'error': 'Invalid URL format. Please provide a complete URL with protocol (http/https)',
                'success': False
            }), 400
        
        # Add protocol if missing
        if not url.startswith(('http://', 'https://')):
            url = 'https://' + url
        
        # Set headers to mimic a real browser request
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
            'Accept-Encoding': 'gzip, deflate',
            'Connection': 'keep-alive',
        }
        
        # Fetch HTML content with timeout
        app.logger.info(f"Fetching HTML from: {url}")
        response = requests.get(url, headers=headers, timeout=30, allow_redirects=True)
        response.raise_for_status()
        
        # Check if content is HTML
        content_type = response.headers.get('content-type', '').lower()
        if 'text/html' not in content_type:
            app.logger.warning(f"Content type is not HTML: {content_type}")
        
        return jsonify({
            'success': True,
            'url': url,
            'html': response.text,
            'status_code': response.status_code,
            'content_type': response.headers.get('content-type', ''),
            'content_length': len(response.text)
        })
        
    except requests.exceptions.Timeout:
        return jsonify({
            'error': 'Request timeout. The server took too long to respond.',
            'success': False
        }), 408
        
    except requests.exceptions.ConnectionError:
        return jsonify({
            'error': 'Connection error. Unable to connect to the provided URL.',
            'success': False
        }), 503
        
    except requests.exceptions.HTTPError as e:
        return jsonify({
            'error': f'HTTP error: {e.response.status_code} - {e.response.reason}',
            'success': False
        }), e.response.status_code
        
    except requests.exceptions.RequestException as e:
        return jsonify({
            'error': f'Request error: {str(e)}',
            'success': False
        }), 500
        
    except Exception as e:
        app.logger.error(f"Unexpected error: {str(e)}")
        return jsonify({
            'error': 'Internal server error occurred while fetching HTML',
            'success': False
        }), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)