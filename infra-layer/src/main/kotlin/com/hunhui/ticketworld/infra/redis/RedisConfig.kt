package com.hunhui.ticketworld.infra.redis

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int,
    @Value("\${spring.data.redis.password}")
    private val password: String,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration()
        redisStandaloneConfiguration.hostName = host
        redisStandaloneConfiguration.port = port
        redisStandaloneConfiguration.password = RedisPassword.of(password)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerKotlinModule()
                .activateDefaultTyping(
                    BasicPolymorphicTypeValidator
                        .builder()
                        .allowIfBaseType(Any::class.java)
                        .build(),
                    ObjectMapper.DefaultTyping.EVERYTHING,
                    JsonTypeInfo.As.PROPERTY,
                )
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = serializer
        }
    }
}
