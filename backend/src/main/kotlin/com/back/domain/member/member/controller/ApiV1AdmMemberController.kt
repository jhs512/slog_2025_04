package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberWithUsernameDto
import com.back.domain.member.member.service.MemberService
import com.back.standard.page.dto.PageDto
import com.back.standard.search.MemberSearchKeywordTypeV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/adm/members")
@Tag(name = "ApiV1AdmMemberController", description = "API 관리자용 회원 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1AdmMemberController(
    private val memberService: MemberService
) {
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "회원 다건 조회")
    fun items(
        @RequestParam(defaultValue = "username") searchKeywordType: MemberSearchKeywordTypeV1,
        @RequestParam(defaultValue = "") searchKeyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): PageDto<MemberWithUsernameDto> {
        return PageDto(
            memberService.findByPaged(searchKeywordType, searchKeyword, page, pageSize)
                .map {
                    MemberWithUsernameDto(
                        it
                    )
                }
        )
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "회원 단건 조회")
    fun item(
        @PathVariable id: Long
    ): MemberWithUsernameDto {
        return MemberWithUsernameDto(
            memberService.findById(id).get()
        )
    }
}
