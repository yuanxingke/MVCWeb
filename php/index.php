<?php

// Simple PHP implementation mirroring the Flask API
// Provides:
//   GET  /                 -> API info
//   POST /api/fetch-html   -> Fetches HTML from a provided URL

function send_json(array $payload, int $statusCode = 200): void {
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE | JSON_INVALID_UTF8_SUBSTITUTE);
}

function get_request_path(): string {
    $uri = $_SERVER['REQUEST_URI'] ?? '/';
    $path = parse_url($uri, PHP_URL_PATH);
    return $path ?: '/';
}

function get_request_method(): string {
    return strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
}

function validate_and_normalize_url(?string $url): array {
    if ($url === null || trim($url) === '') {
        return [null, 'URL parameter is required'];
    }

    $url = trim($url);

    // Add protocol if missing
    if (!preg_match('/^https?:\/\//i', $url)) {
        $url = 'https://' . $url;
    }

    $parts = parse_url($url);
    if (!isset($parts['scheme']) || !isset($parts['host'])) {
        return [null, 'Invalid URL format. Please provide a complete URL with protocol (http/https)'];
    }

    if (!in_array(strtolower($parts['scheme']), ['http', 'https'], true)) {
        return [null, 'Invalid URL scheme. Only http and https are supported'];
    }

    return [$url, null];
}

function fetch_html_via_curl(string $url): array {
    $ch = curl_init();

    $headers = [
        'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Language: en-US,en;q=0.5',
        'Connection: keep-alive',
    ];

    curl_setopt_array($ch, [
        CURLOPT_URL => $url,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 30,
        CURLOPT_CONNECTTIMEOUT => 10,
        CURLOPT_USERAGENT => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_ENCODING => '', // Accept gzip/deflate
        CURLOPT_HEADER => false,
    ]);

    $body = curl_exec($ch);
    $errno = curl_errno($ch);
    $error = curl_error($ch);
    $statusCode = (int) curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $contentType = (string) curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
    curl_close($ch);

    if ($errno !== 0) {
        return [
            'ok' => false,
            'curl_errno' => $errno,
            'curl_error' => $error,
            'status_code' => 0,
            'content_type' => $contentType,
            'body' => null,
        ];
    }

    // Treat HTTP 4xx/5xx as errors to mirror Python's raise_for_status behavior
    if ($statusCode >= 400) {
        return [
            'ok' => false,
            'curl_errno' => 0,
            'curl_error' => '',
            'status_code' => $statusCode,
            'content_type' => $contentType,
            'body' => $body,
        ];
    }

    return [
        'ok' => true,
        'curl_errno' => 0,
        'curl_error' => '',
        'status_code' => $statusCode,
        'content_type' => $contentType,
        'body' => $body,
    ];
}

$method = get_request_method();
$path = get_request_path();

if ($method === 'GET' && $path === '/') {
    send_json([
        'message' => 'MVCWeb API (PHP)',
        'endpoints' => [
            '/api/fetch-html' => [
                'method' => 'POST',
                'description' => 'Fetch HTML content from a provided URL',
                'parameters' => [
                    'url' => 'The URL to fetch HTML content from',
                ],
            ],
        ],
    ]);
    exit;
}

if ($method === 'POST' && $path === '/api/fetch-html') {
    $rawBody = file_get_contents('php://input');
    $data = json_decode($rawBody ?: '', true);

    if (!is_array($data) || !array_key_exists('url', $data)) {
        send_json([
            'error' => 'URL parameter is required',
            'success' => false,
        ], 400);
        exit;
    }

    [$normalizedUrl, $validationError] = validate_and_normalize_url($data['url'] ?? null);
    if ($validationError !== null) {
        send_json([
            'error' => $validationError,
            'success' => false,
        ], 400);
        exit;
    }

    $result = fetch_html_via_curl($normalizedUrl);

    if ($result['ok'] === true) {
        $body = (string) $result['body'];
        send_json([
            'success' => true,
            'url' => $normalizedUrl,
            'html' => $body,
            'status_code' => $result['status_code'],
            'content_type' => $result['content_type'] ?? '',
            'content_length' => strlen($body),
        ]);
        exit;
    }

    // Map cURL/network errors to HTTP status codes similar to Flask version
    if ($result['curl_errno'] !== 0) {
        $errno = $result['curl_errno'];
        if ($errno === CURLE_OPERATION_TIMEDOUT) {
            send_json([
                'error' => 'Request timeout. The server took too long to respond.',
                'success' => false,
            ], 408);
            exit;
        }
        if ($errno === CURLE_COULDNT_CONNECT || $errno === CURLE_COULDNT_RESOLVE_HOST) {
            send_json([
                'error' => 'Connection error. Unable to connect to the provided URL.',
                'success' => false,
            ], 503);
            exit;
        }

        send_json([
            'error' => 'Request error: ' . ($result['curl_error'] ?: ('cURL error #' . $errno)),
            'success' => false,
        ], 500);
        exit;
    }

    // HTTP error status from target
    $statusFromTarget = $result['status_code'];
    send_json([
        'error' => 'HTTP error: ' . $statusFromTarget,
        'success' => false,
    ], $statusFromTarget >= 400 && $statusFromTarget <= 599 ? $statusFromTarget : 500);
    exit;
}

// Fallback 404 for unknown routes
send_json([
    'success' => false,
    'error' => 'Not Found',
], 404);