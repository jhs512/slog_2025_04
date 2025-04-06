package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberDto
import com.back.domain.member.member.service.MemberService
import com.back.global.exceptions.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "ApiV1MemberController", description = "API 회원 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1MemberController(
    private val memberService: MemberService,
    private val passwordEncoder: PasswordEncoder,
    private val rq: Rq
) {
    data class MemberJoinReqBody(
        @field:NotBlank val username: String,
        @field:NotBlank val password: String,
        @field:NotBlank val nickname: String
    )

    @PostMapping("/join")
    @Transactional
    @Operation(summary = "회원가입")
    fun join(
        @RequestBody @Valid reqBody: MemberJoinReqBody
    ): RsData<MemberDto> {
        val member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname, "")

        return RsData(
            "201-1",
            "${member.name}님 환영합니다. 회원가입이 완료되었습니다.",
            MemberDto(member)
        )
    }


    data class MemberLoginReqBody(
        @field:NotBlank val username: String,
        @field:NotBlank val password: String
    )

    data class MemberLoginResBody(
        val item: MemberDto,
        val apiKey: String,
        val accessToken: String
    )

    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "로그인", description = "apiKey, accessToken을 발급합니다. 해당 토큰들은 쿠키(HTTP-ONLY)로도 전달됩니다.")
    fun login(
        @RequestBody @Valid reqBody: MemberLoginReqBody
    ): RsData<MemberLoginResBody> {
        val member = memberService
            .findByUsername(reqBody.username)
            .orElseThrow {
                ServiceException(
                    "401-1",
                    "존재하지 않는 사용자입니다."
                )
            }

        if (!passwordEncoder.matches(reqBody.password, member.password)) throw ServiceException(
            "401-2",
            "비밀번호가 일치하지 않습니다."
        )

        val accessToken = rq.makeAuthCookies(member)

        return RsData(
            "200-1",
            "${member.name}님 환영합니다.",
            MemberLoginResBody(
                MemberDto(member),
                member.apiKey,
                accessToken
            )
        )
    }


    @DeleteMapping("/logout")
    @Transactional(readOnly = true)
    @Operation(summary = "로그아웃", description = "apiKey, accessToken 토큰을 제거합니다.")
    fun logout(): RsData<Empty> {
        rq.deleteCookie("accessToken")
        rq.deleteCookie("apiKey")

        return RsData(
            "200-1",
            "로그아웃 되었습니다."
        )
    }


    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 정보")
    fun me(): MemberDto {
        val actor = memberService.findById(rq.actor!!.id).get()

        return MemberDto(actor)
    }


    data class MemberModifyMeReqBody(
        val nickname: @NotBlank String
    )

    @PutMapping("/me")
    @Transactional
    @Operation(summary = "내 정보 수정")
    fun modifyMe(
        @RequestBody reqBody: @Valid MemberModifyMeReqBody
    ): RsData<MemberDto> {
        val actor = memberService.findByUsername(rq.actor!!.username).get()

        memberService.modify(actor, reqBody.nickname, "")

        rq.refreshAccessToken(actor)

        return RsData(
            "200-1",
            "회원정보가 수정되었습니다.",
            MemberDto(actor)
        )
    }
}
