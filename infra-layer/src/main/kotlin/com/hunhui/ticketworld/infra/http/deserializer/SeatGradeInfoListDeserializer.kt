package com.hunhui.ticketworld.infra.http.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.hunhui.ticketworld.common.vo.Money
import com.hunhui.ticketworld.domain.kopis.SeatGradeInfo

class SeatGradeInfoListDeserializer : JsonDeserializer<List<SeatGradeInfo>>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): List<SeatGradeInfo> {
        // 전체 문자열을 가져옵니다.
        val text = p.text.trim()

        // "전석무료"인 경우, 단일 항목으로 처리: 가격 0
        if (text.equals("전석무료", ignoreCase = true)) {
            return listOf(SeatGradeInfo(name = "전석무료", price = Money(0)))
        }

        // 각 항목은 ", " (콤마+공백)으로 구분된다고 가정합니다.
        val parts = text.split(", ")
        val result = mutableListOf<SeatGradeInfo>()

        // 각 항목은 "이름 가격원" 형태입니다.
        // 가격 부분은 숫자(콤마 포함) 뒤에 "원"이 붙습니다.
        // 예: "ULTIMATE SPHERES EXPERIENCE 1,080,000원"
        val regex = Regex("""(.+?)\s+([\d,]+)원""")

        for (part in parts) {
            val trimmed = part.trim()
            val matchResult = regex.find(trimmed)
            if (matchResult != null) {
                val (name, priceStr) = matchResult.destructured
                // 콤마 제거 후 숫자로 변환
                val amount = priceStr.replace(",", "").toLongOrNull() ?: 0L
                result.add(SeatGradeInfo(name = name.trim(), price = Money(amount)))
            } else {
                // 만약 정규식 매칭이 되지 않는 경우, 예외 없이 무시하거나 로깅 처리 가능
                // 예를 들어 "전석무료"처럼 들어오는 경우가 있을 수 있음.
                if (trimmed.equals("전석무료", ignoreCase = true)) {
                    result.add(SeatGradeInfo(name = "전석무료", price = Money(0)))
                }
            }
        }
        return result
    }
}
