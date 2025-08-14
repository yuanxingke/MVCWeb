<?php
// Basic PHP implementation of the MVCWeb API

declare(strict_types=1);

// Ensure all responses are JSON
function send_json(array $data, int $status = 200): void {
	http_response_code($status);
	header('Content-Type: application/json; charset=utf-8');
	echo json_encode($data, JSON_UNESCAPED_UNICODE);
	exit;
}

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);

if ($path === '/' && $method === 'GET') {
	send_json([
		"message" => "MVCWeb API (PHP)",
		"endpoints" => [
			"/api/fetch-html" => [
				"method" => "POST",
				"description" => "Fetch HTML content from a provided URL",
				"parameters" => [
					"url" => "The URL to fetch HTML content from"
				]
			]
		]
	]);
}

if ($path === '/api/fetch-html' && $method === 'POST') {
	$rawBody = file_get_contents('php://input');
	$data = json_decode($rawBody ?: '', true);

	if (!is_array($data) || !array_key_exists('url', $data)) {
		send_json([
			'error' => 'URL parameter is required',
			'success' => false
		], 400);
	}

	$url = trim((string)$data['url']);

	// Auto-add protocol if missing, then validate
	if (!preg_match('#^https?://#i', $url)) {
		$url = 'https://' . $url;
	}

	$parts = parse_url($url);
	if (!$parts || empty($parts['host'])) {
		send_json([
			'error' => 'Invalid URL format. Please provide a complete URL with protocol (http/https)',
			'success' => false
		], 400);
	}

	$userAgent = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36';
	$headers = [
		'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
		'Accept-Language: en-US,en;q=0.5',
		'Accept-Encoding: gzip, deflate',
		'Connection: keep-alive'
	];

	$ch = curl_init();
	curl_setopt_array($ch, [
		CURLOPT_URL => $url,
		CURLOPT_RETURNTRANSFER => true,
		CURLOPT_FOLLOWLOCATION => true,
		CURLOPT_MAXREDIRS => 10,
		CURLOPT_TIMEOUT => 30,
		CURLOPT_CONNECTTIMEOUT => 10,
		CURLOPT_USERAGENT => $userAgent,
		CURLOPT_HTTPHEADER => $headers,
		CURLOPT_SSL_VERIFYPEER => true,
		CURLOPT_SSL_VERIFYHOST => 2,
		CURLOPT_ENCODING => '' // allow gzip/deflate
	]);

	$responseBody = curl_exec($ch);
	$curlErrNo = curl_errno($ch);
	$curlErrMsg = curl_error($ch);
	$httpStatus = (int)curl_getinfo($ch, CURLINFO_HTTP_CODE);
	$contentType = (string)curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
	curl_close($ch);

	if ($responseBody === false) {
		// Handle specific cURL errors
		if (in_array($curlErrNo, [CURLE_OPERATION_TIMEDOUT], true)) {
			send_json([
				'error' => 'Request timeout. The server took too long to respond.',
				'success' => false
			], 408);
		}

		if (in_array($curlErrNo, [CURLE_COULDNT_CONNECT, CURLE_COULDNT_RESOLVE_HOST, CURLE_COULDNT_RESOLVE_PROXY], true)) {
			send_json([
				'error' => 'Connection error. Unable to connect to the provided URL.',
				'success' => false
			], 503);
		}

		send_json([
			'error' => 'Request error: ' . ($curlErrMsg !== '' ? $curlErrMsg : 'Unknown error'),
			'success' => false
		], 500);
	}

	if ($httpStatus >= 400) {
		send_json([
			'error' => 'HTTP error: ' . $httpStatus,
			'success' => false
		], $httpStatus);
	}

	// Warn if not HTML (still return payload similar to Python app)
	if (stripos($contentType, 'text/html') === false) {
		error_log('Content type is not HTML: ' . $contentType);
	}

	send_json([
		'success' => true,
		'url' => $url,
		'html' => $responseBody,
		'status_code' => $httpStatus,
		'content_type' => $contentType,
		'content_length' => strlen($responseBody)
	]);
}

// Fallback 404
send_json([
	'error' => 'Not found',
	'success' => false
], 404);