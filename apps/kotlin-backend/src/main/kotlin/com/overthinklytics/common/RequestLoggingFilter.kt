package com.overthinklytics.common

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader("X-Request-ID")?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        MDC.put("correlation_id", requestId)
        try {
            response.setHeader("X-Request-ID", requestId)
            filterChain.doFilter(request, response)
        } finally {
            // Simple access log after chain
            logger.info {
                mapOf(
                    "event" to "http_request",
                    "method" to request.method,
                    "path" to request.requestURI,
                    "status" to response.status,
                    "correlation_id" to requestId
                ).toString()
            }
            MDC.clear()
        }
    }
}
