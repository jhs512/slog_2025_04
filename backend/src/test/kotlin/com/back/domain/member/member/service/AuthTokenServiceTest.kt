package com.back.domain.member.member.service

import com.back.standard.util.Ut.jwt.isValid
import com.back.standard.util.Ut.jwt.payload
import com.back.standard.util.Ut.jwt.toString
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Value("\${custom.jwt.secretKey}")
    private lateinit var jwtSecretKey: String

    @Value("\${custom.accessToken.expirationSeconds}")
    private var accessTokenExpirationSeconds: Long = 0

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    fun t1() {
        Assertions.assertThat(authTokenService).isNotNull()
    }

    @Test
    @DisplayName("jjwt 로 JWT 생성, {name=\"Paul\", age=23}")
    fun t2() {
        val issuedAt = Date()
        val expiration = Date(issuedAt.time + 1000L * accessTokenExpirationSeconds!!)

        val secretKey = Keys.hmacShaKeyFor(jwtSecretKey.toByteArray())

        val payload = java.util.Map.of<String, Any?>(
            "name", "Paul",
            "age", 23
        )

        val jwtStr = Jwts.builder()
            .claims(payload)
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()

        Assertions.assertThat(jwtStr).isNotBlank()

        // 키가 유효한지 테스트
        val parsedPayload = Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parse(jwtStr)
            .payload as Map<String, Any>

        // 키로 부터 payload 를 파싱한 결과가 원래 payload 와 같은지 테스트
        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    fun t3() {
        val payload = java.util.Map.of<String, Any>("name", "Paul", "age", 23)

        val jwtStr = toString(jwtSecretKey, accessTokenExpirationSeconds!!, payload)

        Assertions.assertThat(jwtStr).isNotBlank()
        Assertions.assertThat(isValid(jwtSecretKey, jwtStr)).isTrue()

        val parsedPayload = payload(jwtSecretKey, jwtStr)

        Assertions.assertThat(parsedPayload).containsAllEntriesOf(payload)
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(member);")
    fun t4() {
        val memberUser1 = memberService.findByUsername("user1").get()

        val accessToken = authTokenService.genAccessToken(memberUser1)

        Assertions.assertThat(accessToken).isNotBlank()

        Assertions.assertThat(isValid(jwtSecretKey, accessToken)).isTrue()

        val parsedPayload = authTokenService.payload(accessToken)

        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(
                java.util.Map.of(
                    "id", memberUser1.id,
                    "username", memberUser1.username
                )
            )

        println("memberUser1 accessToken = $accessToken")
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(memberAdmin);")
    fun t5() {
        val memberAdmin = memberService.findByUsername("admin").get()

        val accessToken = authTokenService.genAccessToken(memberAdmin)

        Assertions.assertThat(accessToken).isNotBlank()

        Assertions.assertThat(isValid(jwtSecretKey, accessToken)).isTrue()

        val parsedPayload = authTokenService.payload(accessToken)

        Assertions.assertThat(parsedPayload)
            .containsAllEntriesOf(
                java.util.Map.of(
                    "id", memberAdmin.id,
                    "username", memberAdmin.username
                )
            )

        println("memberAdmin accessToken = $accessToken")
    }
}