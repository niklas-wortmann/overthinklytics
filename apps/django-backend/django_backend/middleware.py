import logging
import threading
import uuid
from typing import Callable

from django.utils.deprecation import MiddlewareMixin
from django.http import HttpRequest, HttpResponse

# Thread-local storage for correlation id (simple approach)
_local = threading.local()


def get_correlation_id() -> str | None:
    return getattr(_local, "correlation_id", None)


def set_correlation_id(value: str | None) -> None:
    if value is None:
        if hasattr(_local, "correlation_id"):
            delattr(_local, "correlation_id")
    else:
        _local.correlation_id = value


class CorrelationIdFilter(logging.Filter):
    """Inject correlation id into log records if present."""

    def filter(self, record: logging.LogRecord) -> bool:  # type: ignore[override]
        record.correlation_id = get_correlation_id() or "-"
        return True


class CorrelationIdMiddleware(MiddlewareMixin):
    """Assign a correlation id to each request and log request/response summary.

    - Reads X-Request-ID if provided, otherwise generates a UUID4.
    - Stores it in thread-local for logging filter usage.
    - Adds header X-Request-ID to the response.
    - Logs request method, path, status, and duration.
    """

    def __init__(self, get_response: Callable[[HttpRequest], HttpResponse]):
        super().__init__(get_response)
        self.logger = logging.getLogger("django.request")

    def process_request(self, request: HttpRequest):
        req_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
        set_correlation_id(req_id)
        request.correlation_id = req_id  # type: ignore[attr-defined]

    def process_response(self, request: HttpRequest, response: HttpResponse):
        try:
            req_id = getattr(request, "correlation_id", None) or get_correlation_id()
        finally:
            # Ensure we always clear the id
            set_correlation_id(None)
        if req_id:
            response.headers["X-Request-ID"] = req_id
        # Basic access log
        self.logger.info(
            "request",
            extra={
                "event": "http_request",
                "method": getattr(request, "method", "-"),
                "path": getattr(request, "path", "-"),
                "status": getattr(response, "status_code", 0),
                "correlation_id": req_id or "-",
            },
        )
        return response
