package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.standard.util.Ut.jwt.payload
import com.back.standard.util.Ut.jwt.toString
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService(
    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String,

    @Value("\${custom.accessToken.expirationSeconds}")
    private val accessTokenExpirationSeconds: Long
) {
    fun genAccessToken(member: Member): String {
        val id = member.id
        val username = member.username
        val nickname = member.nickname

        return toString(
            jwtSecretKey,
            accessTokenExpirationSeconds,
            java.util.Map.of(
                "id", id,
                "username", username,
                "nickname", nickname,
                "authorities", member.authoritiesAsStringList
            )
        )
    }

    fun payload(accessToken: String): Map<String, Any?>? {
        val parsedPayload = payload(jwtSecretKey, accessToken) ?: return null

        val id = (parsedPayload["id"] as Int).toLong()
        val username = parsedPayload["username"] as String
        val nickname = parsedPayload["nickname"] as String
        val authorities = parsedPayload["authorities"] as List<String>

        return mapOf(
            "id" to id,
            "username" to username,
            "nickname" to nickname,
            "authorities" to authorities
        )
    }
}