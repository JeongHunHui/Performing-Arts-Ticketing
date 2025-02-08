package com.hunhui.ticketworld.infra.http

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.hunhui.ticketworld.common.error.BusinessException
import com.hunhui.ticketworld.domain.performance.exception.KopisErrorCode
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.time.LocalDate

@Component
class KopisApiClient(
    private val restTemplate: RestTemplate,
    @Value("\${kopis.api.base-url}")
    private val baseUrl: String,
    @Value("\${kopis.api.key}")
    private val serviceKey: String,
    private val xmlMapper: XmlMapper,
) {
    private val logger = LogFactory.getLog(KopisApiClient::class.java)

    fun <T> request(
        middleUrl: String,
        queryParams: QueryParams,
        responseClass: Class<T>,
    ): T {
        val url = "$baseUrl$middleUrl?service=$serviceKey${queryParams.toString}"
        try {
            val response: ResponseEntity<String> = restTemplate.getForEntity(url)
            if (!response.statusCode.is2xxSuccessful || response.body == null) {
                logger.error("Failed to request Kopis API\nurl: $url\nresponse: $response")
                throw BusinessException(KopisErrorCode.REQUEST_FAILED)
            }

            // XML 응답 파싱하여 returncode 확인
            val parsedResponse = xmlMapper.readValue(response.body!!, KopisApiErrorResponse::class.java)

            with(parsedResponse.dbs?.first()) {
                if (this != null && returnCode != null) {
                    logger.error("Failed to request Kopis API\nurl: $url\nresponse: $this)")
                    throw BusinessException(KopisErrorCode.REQUEST_FAILED)
                }
            }

            return xmlMapper.readValue(response.body!!, responseClass)
        } catch (e: RestClientException) {
            logger.error("Failed to request Kopis API\nurl: $url\nerror: $e")
            throw BusinessException(KopisErrorCode.REQUEST_FAILED)
        }
    }

    private val QueryParams.toString: String
        get() {
            val filteredParams = filterValues { it != null } // null 값을 제외

            return if (filteredParams.isNotEmpty()) {
                "&" +
                    filteredParams.entries.joinToString("&") { (key, value) ->
                        when (value) {
                            is LocalDate -> "$key=${value.string}"
                            is Boolean -> "$key=${value.string}"
                            else -> "$key=$value"
                        }
                    }
            } else {
                ""
            }
        }

    private val LocalDate.string: String
        get() = "$year${monthValue.toString().padStart(2, '0')}${dayOfMonth.toString().padStart(2, '0')}"

    private val Boolean.string: String
        get() = if (this) "Y" else "N"

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class KopisApiErrorResponse(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "db")
        val dbs: List<Db>?,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Db(
            @JacksonXmlProperty(localName = "returncode")
            val returnCode: String?,
            @JacksonXmlProperty(localName = "errmsg")
            val errMsg: String?,
            @JacksonXmlProperty(localName = "responsetime")
            val responseTime: String?,
        )
    }
}

typealias QueryParams = Map<String, Any?>
