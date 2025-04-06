package com.back.domain.home.home.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.net.InetAddress.getLocalHost
import java.util.stream.Collectors
import java.util.stream.Stream

@Controller
@Tag(name = "HomeController", description = "홈 컨트롤러")
class HomeController {
    @GetMapping(value = ["/"], produces = ["text/html;charset=utf-8"])
    @ResponseBody
    @Operation(summary = "메인 페이지")
    fun main(): String {
        val hostName = getLocalHost().let {
            "${it.hostName}(${it.hostAddress})"
        }

        return "<h1>API 서버 입니다! on ${hostName}</h1>"
    }

    @GetMapping("/session")
    @ResponseBody
    @Operation(summary = "세션 확인")
    fun session(session: HttpSession): String {
        val sessionDump = Stream.iterate(
            session.attributeNames.asIterator(),
            { obj: Iterator<String?> -> obj.hasNext() },
            { it: Iterator<String> -> it }
        ).flatMap { it: Iterator<String> -> Stream.of(it.next()) }
            .map {
                val attributeValue = session.getAttribute(it)
                "$it = $attributeValue"
            }
            .collect(Collectors.joining("\n", "Session Attributes:\n", ""))

        // 완성된 세션 정보 반환
        return sessionDump
    }
}
