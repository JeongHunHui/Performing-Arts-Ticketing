package com.hunhui.ticketworld.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    @Value("\${spring.api.prefix}") private val apiPrefix: String,
    @Value("\${spring.api.version}") private val apiVersion: String,
) : WebMvcConfigurer {
    override fun configurePathMatch(configurer: PathMatchConfigurer) {
        configurer.addPathPrefix("$apiPrefix/$apiVersion") {
            it.isAnnotationPresent(RestController::class.java) && !it.name.contains("springdoc")
        }
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:3000") // 프론트엔드 도메인
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowCredentials(true)
            .allowedHeaders("*")
    }
}
