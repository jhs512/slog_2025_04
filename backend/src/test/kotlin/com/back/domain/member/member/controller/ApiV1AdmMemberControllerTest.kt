package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import com.back.standard.search.MemberSearchKeywordTypeV1
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1AdmMemberControllerTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다건 조회")
    @WithUserDetails("admin")
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members?page=1&pageSize=3")
            )
            .andDo(MockMvcResultHandlers.print())

        val memberPage = memberService
            .findByPaged(1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(memberPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(memberPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val members = memberPage.content

        for (i in members.indices) {
            val member = members[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(member.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].username").value(member.username))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].nickname").value(member.name))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].profileImgUrl")
                        .value(member.profileImgUrlOrDefault)
                )
        }
    }

    @Test
    @DisplayName("다건 조회 with user1, 403")
    @WithUserDetails("user1")
    fun t2() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members?page=1&pageSize=3")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("다건 조회 with searchKeyword=user")
    @WithUserDetails("admin")
    fun t19() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members?page=1&pageSize=3&searchKeyword=user")
            )
            .andDo(MockMvcResultHandlers.print())

        val memberPage = memberService
            .findByPaged(MemberSearchKeywordTypeV1.username, "user", 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(memberPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(memberPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val members = memberPage.content

        for (i in members.indices) {
            val member = members[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(member.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].username").value(member.username))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].nickname").value(member.name))
        }
    }

    @Test
    @DisplayName("단건조회 3")
    @WithUserDetails("admin")
    fun t3() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/adm/members/3")
            )
            .andDo(MockMvcResultHandlers.print())

        val member = memberService.findById(3).get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1AdmMemberController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(member.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(member.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(member.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(member.username))
            .andExpect(MockMvcResultMatchers.jsonPath("$.nickname").value(member.name))
    }
}