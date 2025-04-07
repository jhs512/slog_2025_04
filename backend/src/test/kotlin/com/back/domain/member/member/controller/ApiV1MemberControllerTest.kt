package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("회원가입")
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/join")
                    .content(
                        """
                        {
                            "username": "usernew",
                            "password": "1234",
                            "nickname": "무명"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService.findByUsername("usernew").get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value("${member.nickname}님 환영합니다. 회원가입이 완료되었습니다.")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value(member.nickname))
    }

    @Test
    @DisplayName("회원가입 without username, password, nickname")
    fun t2() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/join")
                    .content(
                        """
                        {
                            "username": "",
                            "password": "",
                            "nickname": ""
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                    nickname-NotBlank-must not be blank
                    password-NotBlank-must not be blank
                    username-NotBlank-must not be blank
                    """.trimIndent()
                )
            )
    }

    @Test
    @DisplayName("회원가입 시 이미 사용중인 username, 409")
    fun t3() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/join")
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": "1234",
                            "nickname": "무명"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("join"))
            .andExpect(MockMvcResultMatchers.status().isConflict())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("409-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("해당 username은 이미 사용중입니다."))
    }

    @Test
    @DisplayName("로그인")
    fun t4() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": "1234"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService.findByUsername("user1").get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${member.nickname}님 환영합니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.item.createDate")
                    .value(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.item.modifyDate")
                    .value(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.item.nickname").value(member.nickname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.apiKey").value(member.apiKey))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").exists())

        resultActions.andExpect { result: MvcResult ->
            val accessTokenCookie = result.response.getCookie("accessToken")!!
            Assertions.assertThat(accessTokenCookie.value).isNotBlank()
            Assertions.assertThat(accessTokenCookie.path).isEqualTo("/")
            Assertions.assertThat(accessTokenCookie.isHttpOnly).isTrue()
            Assertions.assertThat(accessTokenCookie.secure).isTrue()

            val apiKeyCookie = result.response.getCookie("apiKey")!!
            Assertions.assertThat(apiKeyCookie.value).isEqualTo(member.apiKey)
            Assertions.assertThat(apiKeyCookie.path).isEqualTo("/")
            Assertions.assertThat(apiKeyCookie.isHttpOnly).isTrue()
            Assertions.assertThat(apiKeyCookie.secure).isTrue()
        }
    }

    @Test
    @DisplayName("로그인, without username")
    fun t5() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .content(
                        """
                        {
                            "username": "",
                            "password": "1234"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("username-NotBlank-must not be blank"))
    }

    @Test
    @DisplayName("로그인, without password")
    fun t6() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": ""
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("password-NotBlank-must not be blank"))
    }

    @Test
    @DisplayName("로그인, with wrong username")
    fun t7() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .content(
                        """
                        {
                            "username": "user0",
                            "password": "1234"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 사용자입니다."))
    }

    @Test
    @DisplayName("로그인, with wrong password")
    fun t8() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/members/login")
                    .content(
                        """
                        {
                            "username": "user1",
                            "password": "1"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("login"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."))
    }

    @Test
    @DisplayName("내 정보, with user1 cookies")
    fun t9() {
        val actor = memberService.findByUsername("user2").get()
        val accessToken = memberService.genAccessToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .cookie(
                        Cookie("apiKey", actor.apiKey),
                        Cookie("accessToken", accessToken)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(actor.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.nickname").value(actor.nickname))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(actor.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(actor.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.profileImgUrl").value(actor.profileImgUrlOrDefault))
    }

    @Test
    @DisplayName("내 정보, with user2")
    fun t10() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("me"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(actor.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.nickname").value(actor.nickname))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(actor.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(actor.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.profileImgUrl").value(actor.profileImgUrlOrDefault))
    }

    @Test
    @DisplayName("내 정보, with wrong access key")
    fun t11() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/members/me")
                    .header("Authorization", "Bearer wrong-access-key")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("logout")
    fun t12() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/members/logout")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect { result: MvcResult ->
                val accessTokenCookie = result.response.getCookie("accessToken")!!
                Assertions.assertThat(accessTokenCookie.value).isEmpty()
                Assertions.assertThat(accessTokenCookie.maxAge).isEqualTo(0)
                Assertions.assertThat(accessTokenCookie.path).isEqualTo("/")
                Assertions.assertThat(accessTokenCookie.isHttpOnly).isTrue()
                Assertions.assertThat(accessTokenCookie.secure).isTrue()

                val apiKeyCookie = result.response.getCookie("apiKey")!!
                Assertions.assertThat(apiKeyCookie.value).isEmpty()
                Assertions.assertThat(apiKeyCookie.maxAge).isEqualTo(0)
                Assertions.assertThat(apiKeyCookie.path).isEqualTo("/")
                Assertions.assertThat(apiKeyCookie.isHttpOnly).isTrue()
                Assertions.assertThat(apiKeyCookie.secure).isTrue()
            }
    }

    @Test
    @DisplayName("me/edit")
    @WithUserDetails("user1")
    fun t13() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/members/me")
                    .content(
                        """
                        {
                            "nickname": "새별명"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원정보가 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.modifyDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.nickname").value("새별명"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.profileImgUrl").exists())

        resultActions.andExpect { result: MvcResult ->
            val accessTokenCookie = result.response.getCookie("accessToken")!!
            Assertions.assertThat(accessTokenCookie.value).isNotBlank()
            Assertions.assertThat(accessTokenCookie.path).isEqualTo("/")
            Assertions.assertThat(accessTokenCookie.isHttpOnly).isTrue()
            Assertions.assertThat(accessTokenCookie.secure).isTrue()

            val authorization = result.response.getHeader("Authorization")
            Assertions.assertThat(authorization).isNotBlank()
        }
    }
}