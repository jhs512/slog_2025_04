package com.back.global.app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "custom")
class CustomConfigProperties(
    val notProdMembers: List<NotProdMember>
) {
    data class NotProdMember(val username: String, val nickname: String, val profileImgUrl: String) {
        fun apiKey(): String {
            return username
        }
    }
}
