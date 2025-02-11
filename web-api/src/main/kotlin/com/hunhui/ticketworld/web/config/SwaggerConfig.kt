package com.hunhui.ticketworld.web.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI()
            .components(Components())
            .info(apiInfo())

    private fun apiInfo() =
        Info()
            .title("티켓월드 API 문서")
            .version("1.0.0")
}
