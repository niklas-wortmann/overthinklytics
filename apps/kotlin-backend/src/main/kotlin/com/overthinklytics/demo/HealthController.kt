package com.overthinklytics.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class HealthResponse(val status: String, val backend: String)

@RestController
@RequestMapping("/health")
class HealthController {
    @GetMapping
    fun getHealth(): ResponseEntity<HealthResponse> =
        ResponseEntity.ok(HealthResponse("ok", "kotlin"))
}
