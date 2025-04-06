package com.back.domain.post.post.controller

import com.back.domain.post.post.dto.PostDto
import com.back.domain.post.post.dto.PostWithContentDto
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.service.PostService
import com.back.global.exceptions.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import com.back.standard.page.dto.PageDto
import com.back.standard.search.PostSearchKeywordTypeV1
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "ApiV1PostController", description = "API 글 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostController(
    private val postService: PostService,
    private val rq: Rq
) {
    private fun makePostWithContentDto(post: Post): PostWithContentDto {
        val actor = rq.actor

        val postWithContentDto = PostWithContentDto(post)

        if (actor != null) {
            postWithContentDto.actorCanModify = post.getCheckActorCanModifyRs(actor).isSuccess
            postWithContentDto.actorCanDelete = post.getCheckActorCanDeleteRs(actor).isSuccess
        }

        return postWithContentDto
    }


    data class PostStatisticsResBody(
        val totalPostCount: Long,
        val totalPublishedPostCount: Long,
        val totalListedPostCount: Long
    )

    @GetMapping("/statistics")
    @Transactional(readOnly = true)
    @Operation(summary = "통계정보")
    fun statistics(): PostStatisticsResBody {
        return PostStatisticsResBody(
            postService.count(),
            postService.countByPublished(true),
            postService.countByListed(true)
        )
    }

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    @Operation(summary = "내글 다건 조회")
    fun mine(
        @RequestParam(defaultValue = "title") searchKeywordType: PostSearchKeywordTypeV1,
        @RequestParam(defaultValue = "") searchKeyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "30") pageSize: Int
    ): PageDto<PostDto> {
        val actor = rq.actor!!

        return PageDto(
            postService.findByAuthorPaged(actor, searchKeywordType, searchKeyword, page, pageSize)
                .map { PostDto(it) }
        )
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "공개글 다건 조회")
    fun items(
        @RequestParam(defaultValue = "title") searchKeywordType: PostSearchKeywordTypeV1,
        @RequestParam(defaultValue = "") searchKeyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "30") pageSize: Int
    ): PageDto<PostDto> {
        return PageDto(
            postService.findByListedPaged(true, searchKeywordType, searchKeyword, page, pageSize)
                .map { PostDto(it) }
        )
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건 조회", description = "비밀글은 작성자만 조회 가능")
    fun item(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "") lastModifyDateAfter: LocalDateTime?
    ): PostWithContentDto {
        val post = postService.findById(id).get()

        if (lastModifyDateAfter != null && !post.modifyDate.isAfter(lastModifyDateAfter)) {
            throw ServiceException("412-1", "변경된 데이터가 없습니다.")
        }

        if (!post.isPublished()) {
            val actor = rq.actor ?: throw ServiceException("401-1", "비밀글 입니다. 로그인 후 이용해주세요.")

            post.checkActorCanRead(actor)
        }

        return makePostWithContentDto(post)
    }


    @Transactional
    @PostMapping("/temp")
    @Operation(summary = "임시 글 생성")
    fun makeTemp(): RsData<PostDto> {
        val findTempOrMakeRsData = postService.findTempOrMake(rq.actor!!)

        return findTempOrMakeRsData.newDataOf(
            PostDto(findTempOrMakeRsData.data)
        )
    }


    data class PostWriteReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val title: String,
        @field:NotBlank @field:Size(min = 2, max = 10000000) val content: String,
        val published: Boolean,
        val listed: Boolean
    )

    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    fun write(
        @RequestBody @Valid reqBody: PostWriteReqBody
    ): RsData<PostDto> {
        val actor = rq.actor!!

        val post = postService.write(
            actor,
            reqBody.title,
            reqBody.content,
            reqBody.published,
            reqBody.listed
        )

        return RsData(
            "201-1",
            "${post.id}번 글이 작성되었습니다.",
            PostDto(post)
        )
    }


    data class PostModifyReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val title: String,
        @field:NotBlank @field:Size(min = 2, max = 10000000) val content: String,
        val published: Boolean,
        val listed: Boolean
    )

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    fun modify(
        @PathVariable id: Long,
        @RequestBody @Valid reqBody: PostModifyReqBody
    ): RsData<PostDto> {
        val actor = rq.actor!!

        val post = postService.findById(id).get()

        post.checkActorCanModify(actor)

        postService.modify(post, reqBody.title, reqBody.content, reqBody.published, reqBody.listed)

        postService.flush()

        return RsData(
            "200-1",
            "${id}번 글이 수정되었습니다.",
            PostDto(post)
        )
    }


    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제", description = "작성자 본인 뿐 아니라 관리자도 삭제 가능")
    fun delete(
        @PathVariable id: Long
    ): RsData<Empty> {
        val member = rq.actor!!

        val post = postService.findById(id).get()

        post.checkActorCanDelete(member)

        postService.delete(post)

        return RsData("200-1", "${id}번 글이 삭제되었습니다.")
    }
}


