import json
import types
import requests
import builtins

import app as app_module


def create_test_client():
    return app_module.app.test_client()


class DummyResponse:
    def __init__(self, text: str, status_code: int = 200, headers: dict | None = None):
        self.text = text
        self.status_code = status_code
        self.headers = headers or {"content-type": "text/html; charset=utf-8"}

    def raise_for_status(self):
        if 400 <= self.status_code:
            http_error = requests.exceptions.HTTPError()
            http_error.response = types.SimpleNamespace(status_code=self.status_code, reason="Error")
            raise http_error


def test_index_returns_api_info():
    client = create_test_client()
    resp = client.get("/")
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["message"].startswith("MVCWeb API")
    assert "/api/fetch-html" in data["endpoints"]


def test_fetch_html_missing_url():
    client = create_test_client()
    resp = client.post("/api/fetch-html", json={})
    assert resp.status_code == 400
    data = resp.get_json()
    assert data["success"] is False
    assert "URL parameter is required" in data["error"]


def test_fetch_html_invalid_url_format():
    client = create_test_client()
    # No scheme and no netloc triggers invalid format per current implementation
    resp = client.post("/api/fetch-html", json={"url": "example.com"})
    assert resp.status_code == 400
    data = resp.get_json()
    assert data["success"] is False
    assert "Invalid URL format" in data["error"]


def test_fetch_html_success_html(monkeypatch):
    client = create_test_client()

    def fake_get(url, headers=None, timeout=None, allow_redirects=True):
        return DummyResponse("<html><body>OK</body></html>", 200, {"content-type": "text/html; charset=utf-8"})

    monkeypatch.setattr(app_module.requests, "get", fake_get)

    resp = client.post("/api/fetch-html", json={"url": "https://example.com"})
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["success"] is True
    assert data["status_code"] == 200
    assert data["content_type"].startswith("text/html")
    assert data["content_length"] == len(data["html"])


def test_fetch_html_timeout(monkeypatch):
    client = create_test_client()

    def fake_get(url, headers=None, timeout=None, allow_redirects=True):
        raise requests.exceptions.Timeout()

    monkeypatch.setattr(app_module.requests, "get", fake_get)

    resp = client.post("/api/fetch-html", json={"url": "https://example.com"})
    assert resp.status_code == 408
    data = resp.get_json()
    assert data["success"] is False
    assert "timeout" in data["error"].lower()


def test_fetch_html_connection_error(monkeypatch):
    client = create_test_client()

    def fake_get(url, headers=None, timeout=None, allow_redirects=True):
        raise requests.exceptions.ConnectionError()

    monkeypatch.setattr(app_module.requests, "get", fake_get)

    resp = client.post("/api/fetch-html", json={"url": "https://example.com"})
    assert resp.status_code == 503
    data = resp.get_json()
    assert data["success"] is False
    assert "connection error" in data["error"].lower()


def test_fetch_html_http_error(monkeypatch):
    client = create_test_client()

    class ErrorResponse(DummyResponse):
        def __init__(self):
            super().__init__("Not Found", 404, {"content-type": "text/html"})

        def raise_for_status(self):
            http_error = requests.exceptions.HTTPError()
            http_error.response = types.SimpleNamespace(status_code=404, reason="Not Found")
            raise http_error

    def fake_get(url, headers=None, timeout=None, allow_redirects=True):
        return ErrorResponse()

    monkeypatch.setattr(app_module.requests, "get", fake_get)

    resp = client.post("/api/fetch-html", json={"url": "https://example.com/missing"})
    assert resp.status_code == 404
    data = resp.get_json()
    assert data["success"] is False
    assert "404" in data["error"] and "Not Found" in data["error"]


def test_fetch_html_non_html_content(monkeypatch):
    client = create_test_client()

    def fake_get(url, headers=None, timeout=None, allow_redirects=True):
        return DummyResponse("{}", 200, {"content-type": "application/json"})

    monkeypatch.setattr(app_module.requests, "get", fake_get)

    resp = client.post("/api/fetch-html", json={"url": "https://api.example.com"})
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["success"] is True
    assert data["content_type"].startswith("application/json")