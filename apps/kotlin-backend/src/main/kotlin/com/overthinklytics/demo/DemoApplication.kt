package com.overthinklytics.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication(scanBasePackages = ["com.overthinklytics"])
@EnableJpaRepositories(basePackages = ["com.overthinklytics.analytics.repository"])
@EntityScan(basePackages = ["com.overthinklytics.analytics.entity"])
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
